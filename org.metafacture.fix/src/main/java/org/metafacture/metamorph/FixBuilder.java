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

import org.metafacture.fix.fix.Do;
import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.fix.MethodCall;
import org.metafacture.fix.fix.Options;
import org.metafacture.metamorph.api.Collect;
import org.metafacture.metamorph.api.FlushListener;
import org.metafacture.metamorph.api.InterceptorFactory;
import org.metafacture.metamorph.api.NamedValuePipe;
import org.metafacture.metamorph.collectors.Choose;
import org.metafacture.metamorph.collectors.Combine;
import org.metafacture.metamorph.collectors.Group;
import org.metafacture.metamorph.functions.Compose;
import org.metafacture.metamorph.functions.Constant;
import org.metafacture.metamorph.functions.Equals;
import org.metafacture.metamorph.functions.Lookup;
import org.metafacture.metamorph.functions.NotEquals;
import org.metafacture.metamorph.functions.Regexp;
import org.metafacture.metamorph.functions.Replace;
import org.metafacture.metamorph.maps.FileMap;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Pair;

import java.util.Arrays;
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
    private static final String FLUSH_WITH = "flushWith";
    private static final String RECORD = "record";
    private final Deque<StackFrame> stack = new LinkedList<>();
    private final InterceptorFactory interceptorFactory;
    private final Metafix metafix;
    private CollectFactory collectFactory;

    public FixBuilder(final Metafix metafix, final InterceptorFactory interceptorFactory) {
        this.metafix = metafix;
        this.interceptorFactory = interceptorFactory;

        stack.push(new StackFrame(metafix));
        collectFactory = new CollectFactory();
        collectFactory.registerClass("combine", Combine.class);
        collectFactory.registerClass("choose", Choose.class);
        collectFactory.registerClass("group", Group.class);
    }

    public void walk(final Fix fix, final Map<String, String> vars) {
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
                collect = createEntity(firstParam + "[]");
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

    protected final Map<String, String> resolvedAttributeMap(final EList<String> params, final Options options) {
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("name", resolvedAttribute(params, 1));
        attributes.put("value", resolvedAttribute(params, 2));
        if (options == null) {
            return attributes;
        }
        final EList<String> keys = options.getKeys();
        final EList<String> values = options.getValues();
        for (int i = 0; i < keys.size() && i < values.size(); i = i + 1) {
            attributes.put(keys.get(i), values.get(i));
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

    private void registerFlush(final String flushWith, final Collect flushListener) {
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
        final boolean standalone = Exp.valueOf(expression.getName().toUpperCase()).apply(this, expression, params, source);
        if (standalone) {
            exitData();
            mapBack(source, true);
        }
    }

    private enum Exp {
        MAP {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final NamedValuePipe enterDataMap = builder.enterDataMap(params, false);
                builder.exitData();
                if (enterDataMap instanceof Entity) {
                    builder.exitCollectorAndFlushWith(null);
                }
                return false;
            }
        },
        ADD_FIELD {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                builder.enterDataAdd(params);
                builder.exitData();
                return false;
            }
        },
        REPLACE_ALL {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final Replace replace = new Replace();
                final String thirdParam = builder.resolvedAttribute(params, 3);
                final boolean standalone = thirdParam != null;
                final List<String> p = standalone ? params.subList(1, params.size()) : params;
                replace.setPattern(builder.resolvedAttribute(p, 1));
                replace.setWith(builder.resolvedAttribute(p, 2));
                builder.enterDataFunction(source, replace, standalone);
                return standalone;
            }
        },
        APPEND {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final boolean standalone = params.size() > 1;
                final List<String> p = standalone ? params.subList(1, params.size()) : params;
                builder.compose(source, "", builder.resolvedAttribute(p, 1), standalone);
                return standalone;
            }
        },
        PREPEND {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final boolean standalone = params.size() > 1;
                final List<String> p = standalone ? params.subList(1, params.size()) : params;
                builder.compose(source, builder.resolvedAttribute(p, 1), "", standalone);
                return standalone;
            }
        },
        EQUALS {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final Equals eq = new Equals();
                eq.setString(builder.resolvedAttribute(params, 1));
                builder.enterDataFunction(source, eq, false);
                return false;
            }
        },
        NOT_EQUALS {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final NotEquals neq = new NotEquals();
                neq.setString(builder.resolvedAttribute(params, 1));
                builder.enterDataFunction(source, neq, false);
                return false;
            }
        },
        REGEXP {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final Regexp regexp = new Regexp();
                regexp.setMatch(builder.resolvedAttribute(params, 1));
                builder.enterDataFunction(source, regexp, false);
                return false;
            }
        },
        LOOKUP {
            public boolean apply(final FixBuilder builder, final Expression expression, final List<String> params, final String source) {
                final boolean standalone = builder.resolvedAttribute(params, 1) != null;
                final Lookup lookup = new Lookup();
                lookup.setMaps(builder.metafix);
                final Map<String, String> map = buildMap(expression);
                final String name = map.hashCode() + "";
                lookup.setMap(name);
                builder.metafix.putMap(name, map);
                builder.enterDataFunction(source, lookup, standalone);
                return standalone;
            }

            private Map<String, String> buildMap(final Expression expression) {
                final Map<String, String> options = options((MethodCall) expression);
                final String file = options.get("in");
                final String sep = "separator";
                final boolean useFileMap = file != null && (options.size() == 1 || options.size() == 2 && options.containsKey(sep));
                final Map<String, String> map = useFileMap ? fileMap(file, options.get(sep)) : options;
                return map;
            }

            private Map<String, String> options(final MethodCall method) {
                final Options options = method.getOptions();
                final Map<String, String> map = new HashMap<>();
                if (options != null) {
                    for (int i = 0; i < options.getKeys().size(); i += 1) {
                        map.put(options.getKeys().get(i), options.getValues().get(i));
                    }
                }
                return map;
            }

            private Map<String, String> fileMap(final String secondParam, final String separator) {
                final FileMap fileMap = new FileMap();
                if (separator != null) {
                    fileMap.setSeparator(separator);
                }
                fileMap.setFile(secondParam);
                return fileMap;
            }

        };
        public abstract boolean apply(FixBuilder builder, Expression expression, List<String> params, String source);
    }

    private void compose(final String field, final String prefix, final String postfix, final boolean standalone) {
        final Compose compose = new Compose();
        compose.setPrefix(prefix);
        compose.setPostfix(postfix);
        enterDataFunction(field, compose, standalone);
    }

    private void mapBack(final String name, final boolean standalone) {
        enterDataMap(new BasicEList<String>(Arrays.asList("@" + name, name)), standalone);
        exitData();
    }

    private void exitData() {
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

    private NamedValuePipe enterDataMap(final List<String> params, final boolean standalone) {
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

    private void enterDataAdd(final List<String> params) {
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

    private void enterDataFunction(final String fieldName, final NamedValuePipe function, final boolean standalone) {
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
        // TODO: resolve from vars/map/etc
        return params.size() < i ? null : params.get(i - 1);
    }

    private String resolvedAttribute(final Map<String, String> attributes, final String string) {
        // TODO: resolve from vars/map/etc
        return attributes.get(string);
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
