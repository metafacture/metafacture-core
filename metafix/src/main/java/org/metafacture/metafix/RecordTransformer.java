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
            if (e instanceof Do) {
                processDo((Do) e);
            }
            else if (e instanceof If) {
                processIf((If) e);
            }
            else if (e instanceof Unless) {
                processUnless((Unless) e);
            }
            else if (e instanceof MethodCall) {
                processFunction((MethodCall) e);
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

    private void processDo(final Do expression) {
        processFix(() -> executionExceptionMessage(expression), () -> {
            final FixContext context = getInstance(expression.getName(), FixContext.class, FixBind::valueOf);
            final RecordTransformer recordTransformer = new RecordTransformer(metafix, expression.getElements());

            return record -> context.execute(metafix, record, params(expression.getParams()), options(expression.getOptions()), recordTransformer);
        });
    }

    private void processIf(final If ifExpression) {
        final ElsIf elseIfExpression = ifExpression.getElseIf();
        final Else elseExpression = ifExpression.getElse();

        final Supplier<String> elseIfMessageSupplier = () -> executionExceptionMessage(elseIfExpression, elseIfExpression.eResource());
        final Supplier<String> elseMessageSupplier = () -> executionExceptionMessage(elseExpression, elseExpression.eResource());

        processFix(() -> executionExceptionMessage(ifExpression, ifExpression.eResource()), () -> {
            final FixPredicate ifPredicate = getInstance(ifExpression.getName(), FixPredicate.class, FixConditional::valueOf);
            final FixPredicate elseIfPredicate = elseIfExpression != null ? getInstance(elseIfExpression.getName(), FixPredicate.class, FixConditional::valueOf) : null;

            final RecordTransformer ifTransformer = new RecordTransformer(metafix, ifExpression.getElements());
            final RecordTransformer elseIfTransformer = elseIfExpression != null ? new RecordTransformer(metafix, elseIfExpression.getElements()) : null;
            final RecordTransformer elseTransformer = elseExpression != null ? new RecordTransformer(metafix, elseExpression.getElements()) : null;

            return record -> {
                if (ifPredicate.test(metafix, record, params(ifExpression.getParams()), options(null))) { // TODO: options
                    ifTransformer.transform(record);
                }
                else {
                    if (elseIfExpression != null) {
                        currentMessageSupplier = elseIfMessageSupplier;

                        if (elseIfPredicate.test(metafix, record, params(elseIfExpression.getParams()), options(null))) { // TODO: options
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

    private void processUnless(final Unless expression) {
        processFix(() -> executionExceptionMessage(expression, expression.eResource()), () -> {
            final FixPredicate predicate = getInstance(expression.getName(), FixPredicate.class, FixConditional::valueOf);
            final RecordTransformer recordTransformer = new RecordTransformer(metafix, expression.getElements());

            return record -> {
                if (!predicate.test(metafix, record, params(expression.getParams()), options(null))) { // TODO: options
                    recordTransformer.transform(record);
                }
            };
        });
    }

    private void processFunction(final MethodCall expression) {
        processFix(() -> executionExceptionMessage(expression), () -> {
            final FixFunction function = getInstance(expression.getName(), FixFunction.class, FixMethod::valueOf);
            return record -> function.apply(metafix, record, params(expression.getParams()), options(expression.getOptions()));
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

    private List<String> params(final List<String> params) {
        return params.stream().map(this::resolveVars).collect(Collectors.toList());
    }

    private Map<String, String> options(final Options options) {
        final Map<String, String> map = new LinkedHashMap<>();

        if (options != null) {
            final List<String> keys = options.getKeys();
            final List<String> values = options.getValues();

            for (int i = 0; i < keys.size(); i += 1) {
                map.put(resolveVars(keys.get(i)), resolveVars(values.get(i)));
            }
        }

        return map;
    }

}
