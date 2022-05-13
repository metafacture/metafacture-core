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
import org.metafacture.metafix.fix.Options;
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
            final List<String> params = e.getParams();
            final Map<String, String> options = options(e.getOptions());

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

    private void processDo(final Do expression, final List<String> params, final Map<String, String> options) {
        processFix(() -> executionExceptionMessage(expression), () -> {
            final FixContext context = getInstance(expression.getName(), FixContext.class, FixBind::valueOf);
            final RecordTransformer recordTransformer = new RecordTransformer(metafix, expression.getElements());

            return record -> context.execute(metafix, record, resolveParams(params), resolveOptions(options), recordTransformer);
        });
    }

    private void processIf(final If ifExpression, final List<String> ifParams, final Map<String, String> ifOptions) {
        final ElsIf elseIfExpression = ifExpression.getElseIf();
        final Else elseExpression = ifExpression.getElse();

        final Supplier<String> elseIfMessageSupplier = () -> executionExceptionMessage(elseIfExpression, elseIfExpression.eResource());
        final Supplier<String> elseMessageSupplier = () -> executionExceptionMessage(elseExpression, elseExpression.eResource());

        processFix(() -> executionExceptionMessage(ifExpression, ifExpression.eResource()), () -> {
            final FixPredicate ifPredicate = getInstance(ifExpression.getName(), FixPredicate.class, FixConditional::valueOf);
            final RecordTransformer ifTransformer = new RecordTransformer(metafix, ifExpression.getElements());

            final FixPredicate elseIfPredicate;
            final List<String> elseIfParams;
            final Map<String, String> elseIfOptions;
            final RecordTransformer elseIfTransformer;

            if (elseIfExpression != null) {
                elseIfPredicate = getInstance(elseIfExpression.getName(), FixPredicate.class, FixConditional::valueOf);
                elseIfParams = elseIfExpression.getParams();
                elseIfOptions = options(elseIfExpression.getOptions());
                elseIfTransformer = new RecordTransformer(metafix, elseIfExpression.getElements());
            }
            else {
                elseIfPredicate = null;
                elseIfParams = null;
                elseIfOptions = null;
                elseIfTransformer = null;
            }

            final RecordTransformer elseTransformer = elseExpression != null ? new RecordTransformer(metafix, elseExpression.getElements()) : null;

            return record -> {
                if (ifPredicate.test(metafix, record, resolveParams(ifParams), resolveOptions(ifOptions))) {
                    ifTransformer.transform(record);
                }
                else {
                    if (elseIfExpression != null) {
                        currentMessageSupplier = elseIfMessageSupplier;

                        if (elseIfPredicate.test(metafix, record, resolveParams(elseIfParams), resolveOptions(elseIfOptions))) {
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

    private void processUnless(final Unless expression, final List<String> params, final Map<String, String> options) {
        processFix(() -> executionExceptionMessage(expression, expression.eResource()), () -> {
            final FixPredicate predicate = getInstance(expression.getName(), FixPredicate.class, FixConditional::valueOf);
            final RecordTransformer recordTransformer = new RecordTransformer(metafix, expression.getElements());

            return record -> {
                if (!predicate.test(metafix, record, resolveParams(params), resolveOptions(options))) {
                    recordTransformer.transform(record);
                }
            };
        });
    }

    private void processFunction(final MethodCall expression, final List<String> params, final Map<String, String> options) {
        processFix(() -> executionExceptionMessage(expression), () -> {
            final FixFunction function = getInstance(expression.getName(), FixFunction.class, FixMethod::valueOf);
            return record -> function.apply(metafix, record, resolveParams(params), resolveOptions(options));
        });
    }

    private <T> T getInstance(final String name, final Class<T> baseType, final Function<String, ? extends T> enumFunction) {
        return name.contains(".") ? ReflectionUtil.loadClass(name, baseType).newInstance() : enumFunction.apply(name);
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

    private String resolveVars(final String value) {
        return value == null ? null : StringUtil.format(value, Metafix.VAR_START, Metafix.VAR_END, false, vars);
    }

    private List<String> resolveParams(final List<String> params) {
        final List<String> list = new ArrayList<>(params.size());

        for (final String entry : params) {
            list.add(resolveVars(entry));
        }

        return list;
    }

    private Map<String, String> options(final Options options) {
        final Map<String, String> map = new LinkedHashMap<>();

        if (options != null) {
            final List<String> keys = options.getKeys();
            final List<String> values = options.getValues();

            for (int i = 0; i < keys.size(); i += 1) {
                map.put(keys.get(i), values.get(i));
            }
        }

        return map;
    }

    private Map<String, String> resolveOptions(final Map<String, String> options) {
        final Map<String, String> map = new LinkedHashMap<>(options.size());

        for (final Map.Entry<String, String> entry : options.entrySet()) {
            map.put(resolveVars(entry.getKey()), resolveVars(entry.getValue()));
        }

        return map;
    }

}
