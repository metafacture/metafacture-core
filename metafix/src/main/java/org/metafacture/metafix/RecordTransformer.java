/*
 * Copyright 2021 Fabian Steeg, hbz
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metafacture.metafix;

import org.metafacture.commons.StringUtil;
import org.metafacture.commons.reflection.ReflectionUtil;
import org.metafacture.metafix.api.FixContext;
import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metafix.api.FixPredicate;
import org.metafacture.metafix.fix.Do;
import org.metafacture.metafix.fix.ElsIf;
import org.metafacture.metafix.fix.Else;
import org.metafacture.metafix.fix.Expression;
import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.fix.If;
import org.metafacture.metafix.fix.MethodCall;
import org.metafacture.metafix.fix.Unless;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Transform a record using a {@link Fix}.
 *
 * @author Fabian Steeg
 *
 */
public class RecordTransformer { // checkstyle-disable-line ClassFanOutComplexity

    private static final Logger LOG = LoggerFactory.getLogger(RecordTransformer.class);

    private final List<Consumer<Record>> consumers = new LinkedList<>();
    private final Map<String, String> vars;
    private final Metafix metafix;

    private Supplier<String> currentMessageSupplier;

    /*package-private*/ RecordTransformer(final Metafix metafix, final Fix fix) {
        this(metafix, fix.getElements());
    }

    private RecordTransformer(final Metafix metafix, final List<Expression> expressions) {
        this.metafix = metafix;
        vars = metafix.getVars();

        expressions.forEach(e -> {
            final Params params = new Params(e.getParams(), vars);
            final Options options = new Options(e.getOptions(), vars);

            if (e instanceof Do) {
                processDo((Do) e, params, options);
            }
            else if (e instanceof If) {
                processIf((If) e, params, options);
            }
            else if (e instanceof Unless) {
                processUnless((Unless) e, params, options);
            }
            else if (e instanceof MethodCall) {
                processFunction((MethodCall) e, params, options);
            }
            else {
                throw new FixProcessException(executionExceptionMessage(e));
            }
        });
    }

    public void transform(final Record record) {
        consumers.forEach(consumer -> {
            final FixExecutionException exception = tryRun(() -> consumer.accept(record));

            if (exception != null) {
                metafix.getStrictness().handle(exception, record);
            }
        });
    }

    private void processDo(final Do expression, final Params params, final Options options) {
        processFix(() -> executionExceptionMessage(expression), () -> {
            final FixContext context = getInstance(expression.getName(), FixContext.class, FixBind::valueOf);
            final RecordTransformer recordTransformer = new RecordTransformer(metafix, expression.getElements());

            return record -> context.execute(metafix, record, params.resolve(), options.resolve(), recordTransformer);
        });
    }

    private void processIf(final If ifExpression, final Params ifParams, final Options ifOptions) {
        final List<ElsIf> elseIfExpressions = ifExpression.getElseIf();
        final Else elseExpression = ifExpression.getElse();

        final List<Supplier<String>> elseIfMessageSuppliers = mapList(elseIfExpressions, e -> () -> executionExceptionMessage(e, e.eResource()));
        final Supplier<String> elseMessageSupplier = () -> executionExceptionMessage(elseExpression, elseExpression.eResource());

        processFix(() -> executionExceptionMessage(ifExpression, ifExpression.eResource()), () -> {
            final FixPredicate ifPredicate = getInstance(ifExpression.getName(), FixPredicate.class, FixConditional::valueOf);
            final RecordTransformer ifTransformer = new RecordTransformer(metafix, ifExpression.getElements());

            final List<FixPredicate> elseIfPredicates = mapList(elseIfExpressions, e -> getInstance(e.getName(), FixPredicate.class, FixConditional::valueOf));
            final List<Params> elseIfParamsList = mapList(elseIfExpressions, e -> new Params(e.getParams(), vars));
            final List<Options> elseIfOptionsList = mapList(elseIfExpressions, e -> new Options(e.getOptions(), vars));
            final List<RecordTransformer> elseIfTransformers = mapList(elseIfExpressions, e -> new RecordTransformer(metafix, e.getElements()));

            final RecordTransformer elseTransformer = elseExpression != null ? new RecordTransformer(metafix, elseExpression.getElements()) : null;

            return record -> {
                if (ifPredicate.test(metafix, record, ifParams.resolve(), ifOptions.resolve())) {
                    ifTransformer.transform(record);
                }
                else {
                    for (int i = 0; i < elseIfExpressions.size(); ++i) {
                        currentMessageSupplier = elseIfMessageSuppliers.get(i);

                        final ElsIf elseIfExpression = elseIfExpressions.get(i);

                        final FixPredicate elseIfPredicate = elseIfPredicates.get(i);
                        final Params elseIfParams = elseIfParamsList.get(i);
                        final Options elseIfOptions = elseIfOptionsList.get(i);
                        final RecordTransformer elseIfTransformer = elseIfTransformers.get(i);

                        if (elseIfPredicate.test(metafix, record, elseIfParams.resolve(), elseIfOptions.resolve())) {
                            elseIfTransformer.transform(record);
                            return;
                        }
                    }

                    if (elseExpression != null) {
                        currentMessageSupplier = elseMessageSupplier;
                        elseTransformer.transform(record);
                    }
                }
            };
        });
    }

    private void processUnless(final Unless expression, final Params params, final Options options) {
        processFix(() -> executionExceptionMessage(expression, expression.eResource()), () -> {
            final FixPredicate predicate = getInstance(expression.getName(), FixPredicate.class, FixConditional::valueOf);
            final RecordTransformer recordTransformer = new RecordTransformer(metafix, expression.getElements());

            return record -> {
                if (!predicate.test(metafix, record, params.resolve(), options.resolve())) {
                    recordTransformer.transform(record);
                }
            };
        });
    }

    private void processFunction(final MethodCall expression, final Params params, final Options options) {
        processFix(() -> executionExceptionMessage(expression), () -> {
            final FixFunction function = getInstance(expression.getName(), FixFunction.class, FixMethod::valueOf);
            return record -> function.apply(metafix, record, params.resolve(), options.resolve());
        });
    }

    private <T> T getInstance(final String name, final Class<T> baseType, final Function<String, ? extends T> enumFunction) {
        return name.contains(".") ? ReflectionUtil.loadClass(name, baseType).newInstance() : enumFunction.apply(name);
    }

    private <T, R> List<R> mapList(final List<T> list, final Function<T, R> function) {
        return list.stream().map(function).collect(Collectors.toList());
    }

    private void processFix(final Supplier<String> messageSupplier, final Supplier<Consumer<Record>> consumerSupplier) {
        currentMessageSupplier = messageSupplier;

        final FixExecutionException exception = tryRun(() -> {
            final Consumer<Record> consumer = consumerSupplier.get();

            consumers.add(record -> {
                currentMessageSupplier = messageSupplier;
                consumer.accept(record);
            });
        });

        if (exception != null) {
            throw exception;
        }
    }

    private FixExecutionException tryRun(final Runnable runnable) { // checkstyle-disable-line ReturnCount
        try {
            runnable.run();
        }
        catch (final FixProcessException e) {
            throw e; // TODO: Add nesting information?
        }
        catch (final FixExecutionException e) {
            return e; // TODO: Add nesting information?
        }
        catch (final IllegalStateException | NumberFormatException e) {
            return new FixExecutionException(currentMessageSupplier.get(), e);
        }
        catch (final RuntimeException e) { // checkstyle-disable-line IllegalCatch
            throw new FixProcessException(currentMessageSupplier.get(), e);
        }

        return null;
    }

    private String executionExceptionMessage(final Expression expression) {
        return executionExceptionMessage(expression, expression.eResource());
    }

    private String executionExceptionMessage(final EObject object, final Resource resource) {
        final INode node = NodeModelUtils.getNode(object);

        return String.format("Error while executing Fix expression (at %s, line %d): %s",
                resource.getURI(), node.getStartLine(), NodeModelUtils.getTokenText(node));
    }

    private abstract static class AbstractResolvable<T> {

        protected boolean isResolvable(final String value) {
            return value != null && value.contains(Metafix.VAR_START);
        }

        protected String resolveVars(final String value, final Map<String, String> vars) {
            return value == null ? null : StringUtil.format(value, Metafix.VAR_START, Metafix.VAR_END, false, vars);
        }

        protected abstract T resolve();

    }

    private static class Params extends AbstractResolvable<List<String>> {

        private final List<String> list;
        private final Map<String, String> vars;
        private final boolean resolve;

        private Params(final List<String> list, final Map<String, String> vars) {
            this.list = list;
            this.vars = vars;

            resolve = list.stream().anyMatch(this::isResolvable);
        }

        @Override
        protected List<String> resolve() {
            if (resolve) {
                final List<String> resolvedList = new ArrayList<>(list.size());

                for (final String entry : list) {
                    resolvedList.add(resolveVars(entry, vars));
                }

                return resolvedList;
            }
            else {
                return list;
            }
        }

    }

    private static class Options extends AbstractResolvable<Map<String, String>> {

        private final Map<String, String> map = new LinkedHashMap<>();
        private final Map<String, String> vars;
        private final boolean resolve;

        private Options(final org.metafacture.metafix.fix.Options options, final Map<String, String> vars) {
            this.vars = vars;

            boolean resolveTemp = false;

            if (options != null) {
                final List<String> keys = options.getKeys();
                final List<String> values = options.getValues();

                for (int i = 0; i < keys.size(); ++i) {
                    final String key = keys.get(i);
                    final String value = values.get(i);

                    map.put(key, value);

                    if (!resolveTemp && (isResolvable(key) || isResolvable(value))) {
                        resolveTemp = true;
                    }
                }
            }

            resolve = resolveTemp;
        }

        @Override
        protected Map<String, String> resolve() {
            if (resolve) {
                final Map<String, String> resolvedMap = new LinkedHashMap<>(map.size());

                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    resolvedMap.put(resolveVars(entry.getKey(), vars), resolveVars(entry.getValue(), vars));
                }

                return resolvedMap;
            }
            else {
                return map;
            }
        }

    }

}
