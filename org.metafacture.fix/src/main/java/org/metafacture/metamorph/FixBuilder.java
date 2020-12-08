/*
 * Copyright 2013, 2020 Deutsche Nationalbibliothek and others
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

package org.metafacture.metamorph;

import org.metafacture.commons.StringUtil;
import org.metafacture.fix.fix.Do;
import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.fix.MethodCall;
import org.metafacture.fix.fix.Options;
import org.metafacture.metamorph.api.Collect;
import org.metafacture.metamorph.api.FlushListener;
import org.metafacture.metamorph.api.Function;
import org.metafacture.metamorph.api.InterceptorFactory;
import org.metafacture.metamorph.api.NamedValuePipe;
import org.metafacture.metamorph.functions.Constant;
import org.metafacture.metamorph.functions.NotEquals;
import org.metafacture.metamorph.functions.Replace;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Pair;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Builds a {@link Metafix} from a Fix DSL description
 *
 * @author Markus Michael Geipel (MorphBuilder)
 * @author Christoph Böhme (MorphBuilder)
 * @author Fabian Steeg (FixBuilder)
 *
 */
public class FixBuilder { // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity

    static final String ARRAY_MARKER = "[]";
    private static final String FLUSH_WITH = "flushWith";
    private static final String RECORD = "record";
    private final Deque<StackFrame> stack = new LinkedList<>();
    private final InterceptorFactory interceptorFactory;
    private final Metafix metafix;

    private CollectFactory collectFactory;
    private FunctionFactory functionFactory;

    public FixBuilder(final Metafix metafix, final InterceptorFactory interceptorFactory) {
        this.metafix = metafix;
        this.interceptorFactory = interceptorFactory;

        stack.push(new StackFrame(metafix));

        collectFactory = new CollectFactory();
        functionFactory = new FunctionFactory();
        // morph: not-equals, replace, fix: not_equals, replace_all
        functionFactory.registerClass("not_equals", NotEquals.class);
        functionFactory.registerClass("replace_all", Replace.class);
    }

    Metafix getMetafix() {
        return metafix;
    }

    public void walk(final Fix fix) {
        processSubexpressions(fix.getElements(), null);
    }

    private void processBind(final Expression expression, final EList<String> params) {
        final String firstParam = resolvedAttribute(params, 1);
        final Do theDo = (Do) expression;
        Collect collect = null;

        // Special bind cases, no generic no-args collectors
        switch (expression.getName()) {
            case "entity":
                collect = createEntity(firstParam);
                break;
            case "array":
                collect = createEntity(firstParam + ARRAY_MARKER);
                break;
            case "map":
                final NamedValuePipe enterDataMap = enterDataMap(params, false);
                processSubexpressions(theDo.getElements(), firstParam);
                exitData();
                if (enterDataMap instanceof Entity) {
                    exitCollectorAndFlushWith(firstParam);
                }
                return;
            default:
                break;
        }

        // try generic no-args collectors, registered in collectFactory
        if (collect == null) {
            if (!collectFactory.containsKey(expression.getName())) {
                throw new IllegalArgumentException("Collector " + expression.getName() +
                        " not found");
            }
            final Map<String, String> attributes = resolvedAttributeMap(params, theDo.getOptions());
            // flushWith should not be passed to the headPipe object via a
            // setter (see newInstance):
            attributes.remove(FLUSH_WITH);
            collect = collectFactory.newInstance(expression.getName(), attributes);
        }
        if (collect != null) {
            stack.push(new StackFrame(collect));
            processSubexpressions(theDo.getElements(), firstParam);
            // must be set after recursive calls to flush descendants before parent
            final String flushWith = resolvedAttribute(resolvedAttributeMap(params, theDo.getOptions()), FLUSH_WITH);
            exitCollectorAndFlushWith(flushWith);
        }
    }

    protected final String resolveVars(final String string) {
        return string == null ? null : StringUtil.format(string, Metafix.VAR_START, Metafix.VAR_END, false, metafix.getVars());
    }

    protected final Map<String, String> resolvedAttributeMap(final List<String> params, final Options options) {
        final Map<String, String> attributes = new HashMap<String, String>();
        if (options == null) {
            return attributes;
        }
        final EList<String> keys = options.getKeys();
        final EList<String> values = options.getValues();
        for (int i = 0; i < keys.size() && i < values.size(); i = i + 1) {
            attributes.put(resolveVars(keys.get(i)), resolveVars(values.get(i)));
        }
        return attributes;
    }

    private Collect createEntity(final String name) {
        final Entity entity = new Entity(() -> metafix.getStreamReceiver());
        entity.setName(name);
        return entity;
    }

    protected void exitCollectorAndFlushWith(final String flushWith) {
        final StackFrame currentCollect = stack.pop();
        final NamedValuePipe tailPipe = currentCollect.getPipe();

        final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
        final NamedValuePipe delegate;
        if (interceptor == null || tailPipe instanceof Entity) {
            // The result of entity collectors cannot be intercepted
            // because they only use the receive/emit interface for
            // signalling while the actual data is transferred using
            // a custom mechanism. In order for this to work the Entity
            // class checks whether source and receiver are an
            // instances of Entity. If an interceptor is inserted between
            // entity elements this mechanism will break.
            delegate = tailPipe;
        }
        else {
            delegate = interceptor;
            delegate.addNamedValueSource(tailPipe);
        }

        final StackFrame parent = stack.peek();

        if (parent.isInEntity()) {
            ((Entity) parent.getPipe()).setNameSource(delegate);
        }
        // TODO: condition handling, see MorphBuilder

        parent.getPipe().addNamedValueSource(delegate);

        final Collect collector = (Collect) tailPipe;
        if (null != flushWith) {
            collector.setWaitForFlush(true);
            registerFlush(flushWith, collector);
        }
    }

    private void registerFlush(final String flushWith, final FlushListener flushListener) {
        final String[] keysSplit = Pattern.compile("|", Pattern.LITERAL).split(flushWith);
        for (final String key : keysSplit) {
            final FlushListener interceptor = interceptorFactory.createFlushInterceptor(flushListener);
            final FlushListener delegate;
            if (interceptor == null) {
                delegate = flushListener;
            }
            else {
                delegate = interceptor;
            }
            if (key.equals(RECORD)) {
                metafix.registerRecordEndFlush(delegate);
            }
            else {
                metafix.registerNamedValueReceiver(key, new Flush(delegate));
            }
        }
    }

    private void processSubexpressions(final List<Expression> expressions, final String superSource) {
        for (final Expression sub : expressions) {
            final EList<String> p = sub.getParams();
            String source = resolvedAttribute(p, 1);
            if (source == null && superSource != null) {
                source = superSource;
            }
            if (sub instanceof Do) {
                processBind(sub, p);
            }
            else {
                processFunction(sub, p, source);
            }
        }
    }

    private void processFunction(final Expression expression, final List<String> params, final String source) {
        final FixFunction functionToRun = findFixFunction(expression);
        if (functionToRun != null) {
            functionToRun.apply(this, expression, params, source);
        }
        else {
            runMetamorphFunction(expression, params);
        }
    }

    private FixFunction findFixFunction(final Expression expression) {
        for (final FixFunction exp : FixFunction.values()) {
            if (exp.name().equalsIgnoreCase(expression.getName())) {
                return exp;
            }
        }
        return null;
    }

    private void runMetamorphFunction(final Expression expression, final List<String> params) {
        final Map<String, String> attributes = resolvedAttributeMap(params, ((MethodCall) expression).getOptions());
        if (functionFactory.containsKey(expression.getName())) {
            final String flushWith = attributes.remove(FLUSH_WITH);
            final Function function = functionFactory.newInstance(expression.getName(), attributes);
            if (null != flushWith) {
                registerFlush(flushWith, function);
            }
            function.setMaps(metafix);
            final StackFrame head = stack.peek();
            final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
            final NamedValuePipe delegate;
            if (interceptor == null) {
                delegate = function;
            }
            else {
                delegate = interceptor;
                function.addNamedValueSource(delegate);
            }
            delegate.addNamedValueSource(head.getPipe());
            head.setPipe(function);
        }
        else {
            throw new IllegalArgumentException(expression.getName() + " not found");
        }
    }

    void exitData() {
        final NamedValuePipe dataPipe = stack.pop().getPipe();

        final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
        final NamedValuePipe delegate;
        if (interceptor == null) {
            delegate = dataPipe;
        }
        else {
            delegate = interceptor;
            delegate.addNamedValueSource(dataPipe);
        }

        final StackFrame parent = stack.peek();

        if (parent.isInEntity()) {
            ((Entity) parent.getPipe()).setNameSource(delegate);
        }

        // TODO: condition handling, see MorphBuilder

        parent.getPipe().addNamedValueSource(delegate);
    }

    NamedValuePipe enterDataMap(final List<String> params, final boolean standalone) {
        Entity entity = null;
        final Data data = new Data();
        String dataName = resolvedAttribute(params, 2);
        final String resolvedAttribute = resolvedAttribute(params, 2);
        if (resolvedAttribute != null && resolvedAttribute.contains(".")) {
            final String[] keyElements = resolvedAttribute.split("\\.");
            final Pair<Entity, Entity> firstAndLast = createEntities(keyElements);
            firstAndLast.getValue().addNamedValueSource(data);
            entity = firstAndLast.getKey();
            stack.push(new StackFrame(entity));
            dataName = keyElements[keyElements.length - 1];
        }
        data.setName(dataName);
        final String source = resolvedAttribute(params, 1);
        metafix.registerNamedValueReceiver(source, getDelegate(data));
        final StackFrame frame = new StackFrame(data);
        if (!standalone) {
            frame.setInEntity(true);
        }
        stack.push(frame);
        return entity != null ? entity : data;
    }

    void enterDataAdd(final List<String> params) {
        final String resolvedAttribute = resolvedAttribute(params, 1);
        if (resolvedAttribute.contains(".")) {
            addNestedField(params, resolvedAttribute);
        }
        else {
            final Data newDate = new Data();
            newDate.setName(resolvedAttribute);
            final Constant constant = new Constant();
            constant.setValue(resolvedAttribute(params, 2));
            newDate.addNamedValueSource(constant);
            metafix.registerNamedValueReceiver("_id", constant);
            stack.push(new StackFrame(newDate));
        }
    }

    void enterDataFunction(final String fieldName, final NamedValuePipe function, final boolean standalone) {
        if (standalone) {
            final Data data = new Data();
            data.setName("@" + fieldName);
            metafix.registerNamedValueReceiver(fieldName, getDelegate(data, function));
            stack.push(new StackFrame(data));
            return;
        }
        final StackFrame head = stack.peek();
        final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
        final NamedValuePipe delegate;
        if (interceptor == null) {
            delegate = function;
        }
        else {
            delegate = interceptor;
            function.addNamedValueSource(delegate);
        }
        delegate.addNamedValueSource(head.getPipe());
        head.setPipe(function);
    }

    private void addNestedField(final List<String> params, final String resolvedAttribute) {
        final String[] keyElements = resolvedAttribute.split("\\.");
        final Pair<Entity, Entity> firstAndLast = createEntities(keyElements);
        final Constant constant = new Constant();
        constant.setValue(resolvedAttribute(params, 2));
        final Data newData = new Data();
        newData.setName(keyElements[keyElements.length - 1]);
        newData.addNamedValueSource(constant);
        firstAndLast.getValue().addNamedValueSource(newData);
        metafix.registerNamedValueReceiver("_id", constant);
        stack.push(new StackFrame(firstAndLast.getKey()));
    }

    private Pair<Entity, Entity> createEntities(final String[] keyElements) {
        Entity firstEntity = null;
        Entity lastEntity = null;
        for (int i = 0; i < keyElements.length - 1; i = i + 1) {
            final Entity currentEntity = new Entity(() -> metafix.getStreamReceiver());
            currentEntity.setName(keyElements[i]);
            if (firstEntity == null) {
                firstEntity = currentEntity;
            }
            if (lastEntity != null) {
                lastEntity.addNamedValueSource(currentEntity);
            }
            lastEntity = currentEntity;
        }
        return new Pair<>(firstEntity, lastEntity);
    }

    private NamedValuePipe getDelegate(final NamedValuePipe dataForDelegate) {
        return getDelegate(dataForDelegate, interceptorFactory.createNamedValueInterceptor());
    }

    private NamedValuePipe getDelegate(final NamedValuePipe dataForDelegate, final NamedValuePipe interceptor) {
        final NamedValuePipe delegate;

        if (interceptor == null) {
            delegate = dataForDelegate;
        }
        else {
            delegate = interceptor;
            dataForDelegate.addNamedValueSource(delegate);
        }

        return delegate;
    }

    private String resolvedAttribute(final List<String> params, final int i) {
        return params.size() < i ? null : resolveVars(params.get(i - 1));
    }

    private String resolvedAttribute(final Map<String, String> attributes, final String string) {
        return resolveVars(attributes.get(string));
    }

    private static class StackFrame {

        private NamedValuePipe pipe;

        private boolean inEntity;

        private StackFrame(final NamedValuePipe pipe) {
            this.pipe = pipe;
        }

        public NamedValuePipe getPipe() {
            return pipe;
        }

        public void setPipe(final NamedValuePipe pipe) {
            this.pipe = pipe;
        }

        public void setInEntity(final boolean inEntity) {
            this.inEntity = inEntity;
        }

        public boolean isInEntity() {
            return inEntity;
        }

    }

}
