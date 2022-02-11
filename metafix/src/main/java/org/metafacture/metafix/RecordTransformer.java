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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transform a record using a {@link Fix}.
 *
 * @author Fabian Steeg
 *
 */
public class RecordTransformer { // checkstyle-disable-line ClassFanOutComplexity

    private static final Logger LOG = LoggerFactory.getLogger(RecordTransformer.class);

    private final Metafix metafix;

    private Record record;

    /*package-private*/ RecordTransformer(final Metafix metafix) {
        this.metafix = metafix;
    }

    /*package-private*/ Record transform(final Fix fix) {
        setRecord(metafix.getCurrentRecord().shallowClone());
        return transformRecord(fix);
    }

    public Record transformRecord(final Fix fix) {
        process(fix.getElements());
        return record;
    }

    public void setRecord(final Record record) {
        this.record = record;
    }

    public void process(final List<Expression> expressions) {
        expressions.forEach(e -> {
            final List<String> params = resolveParams(e.getParams());

            if (e instanceof Do) {
                processDo((Do) e, params);
            }
            else if (e instanceof If) {
                processIf((If) e, params);
            }
            else if (e instanceof Unless) {
                processUnless((Unless) e, params);
            }
            else if (e instanceof MethodCall) {
                processFunction((MethodCall) e, params);
            }
            else {
                throw new FixExecutionException("Unknown Fix expression type: " + e);
            }
        });
    }

    private void processDo(final Do expression, final List<String> params) {
        processExpression(expression, name -> {
            final FixContext context = getInstance(name, FixContext.class, FixBind::valueOf);
            context.execute(metafix, record, params, options(expression.getOptions()), expression.getElements());
        });

        // TODO, possibly: use morph collectors here
        // final CollectFactory collectFactory = new CollectFactory();
        // final Map<String, String> attributes = resolvedAttributeMap(params, expression.getOptions());
        // final Collect collect = collectFactory.newInstance(expression.getName(), attributes);
    }

    private void processIf(final If expression, final List<String> params) {
        final ElsIf elseIfExpression = expression.getElseIf();
        final Else elseExpression = expression.getElse();

        if (testConditional(expression.getName(), params)) {
            process(expression.getElements());
        }
        else if (elseIfExpression != null && testConditional(elseIfExpression.getName(), resolveParams(elseIfExpression.getParams()))) {
            process(elseIfExpression.getElements());
        }
        else if (elseExpression != null) {
            process(elseExpression.getElements());
        }
    }

    private void processUnless(final Unless expression, final List<String> params) {
        if (!testConditional(expression.getName(), params)) {
            process(expression.getElements());
        }
    }

    private boolean testConditional(final String conditional, final List<String> params) {
        LOG.debug("<IF>: {} parameters: {}", conditional, params);

        final AtomicBoolean bool = new AtomicBoolean();

        processResource(conditional, name -> {
            final FixPredicate predicate = getInstance(name, FixPredicate.class, FixConditional::valueOf);
            bool.set(predicate.test(metafix, record, params, options(null))); // TODO: options
        });

        return bool.get();

        // TODO, possibly: use morph functions here (& in processFunction):
        // final FunctionFactory functionFactory = new FunctionFactory();
        // functionFactory.registerClass("not_equals", NotEquals.class);
        // functionFactory.registerClass("replace_all", Replace.class);
        // final Function function = functionFactory.newInstance(conditional,
        // resolvedAttributeMap(params, theIf.getOptions()));
    }

    private void processFunction(final MethodCall expression, final List<String> params) {
        processExpression(expression, name -> {
            final FixFunction function = getInstance(name, FixFunction.class, FixMethod::valueOf);
            function.apply(metafix, record, params, options(expression.getOptions()));
        });
    }

    private <T> T getInstance(final String name, final Class<T> baseType, final Function<String, ? extends T> enumFunction) {
        return name.contains(".") ? ReflectionUtil.loadClass(name, baseType).newInstance() : enumFunction.apply(name);
    }

    private void processExpression(final Expression expression, final Consumer<String> consumer) {
        processResource(expression.getName(), consumer);
    }

    private void processResource(final String name, final Consumer<String> consumer) {
        try {
            consumer.accept(name);
        }
        catch (final FixExecutionException e) {
            throw e; // TODO: Add nesting information?
        }
        catch (final RuntimeException e) { // checkstyle-disable-line IllegalCatch
            throw new FixExecutionException("Error while executing Fix expression: " + name, e);
        }
    }

    private List<String> resolveParams(final List<String> params) {
        final Map<String, String> vars = metafix.getVars();

        return params.stream().map(s -> s == null ? null :
                StringUtil.format(s, Metafix.VAR_START, Metafix.VAR_END, false, vars)).collect(Collectors.toList());
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

}
