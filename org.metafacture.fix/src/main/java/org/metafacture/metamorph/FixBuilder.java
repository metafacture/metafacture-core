/*
 * Copyright 2013, 2019 Deutsche Nationalbibliothek and others
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

import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.metamorph.api.ConditionAware;
import org.metafacture.metamorph.api.InterceptorFactory;
import org.metafacture.metamorph.api.NamedValuePipe;

import org.eclipse.emf.common.util.EList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * Builds a {@link Metafix} from an Fix DSL description
 *
 * @author Markus Michael Geipel (MorphBuilder)
 * @author Christoph Böhme (MorphBuilder)
 * @author Fabian Steeg (FixBuilder)
 *
 */
public class FixBuilder {

    private final Deque<StackFrame> stack = new LinkedList<StackFrame>();
    private Metafix metafix;
    private InterceptorFactory interceptorFactory;

    public FixBuilder(final Metafix metafix, final InterceptorFactory interceptorFactory) {
        this.metafix = metafix;
        this.interceptorFactory = interceptorFactory;
        stack.push(new StackFrame(metafix));
    }

    public void walk(final Fix fix, final Map<String, String> vars) {
        final EList<Expression> elements = fix.getElements();
        for (final Expression expression : elements) {
            if (expression.getName().equals("map")) {
                final EList<String> params = expression.getParams();
                enterData(params);
                exitData();
            }
        }
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
        if (parent.isInEntityName()) {
            // Protected xsd schema and by assertion in enterName:
            ((Entity) parent.getPipe()).setNameSource(delegate);
        }
        else if (parent.isInCondition()) {
            // Protected xsd schema and by assertion in enterIf:
            ((ConditionAware) parent.getPipe()).setConditionSource(delegate);
        }
        else {
            parent.getPipe().addNamedValueSource(delegate);
        }
    }

    private void enterData(final EList<String> params) {
        final Data data = new Data();
        data.setName(resolvedAttribute(params, 2));

        final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
        final NamedValuePipe delegate;
        if (interceptor == null) {
            delegate = data;
        }
        else {
            delegate = interceptor;
            data.addNamedValueSource(delegate);
        }

        final String source = resolvedAttribute(params, 1);
        metafix.registerNamedValueReceiver(source, delegate);

        stack.push(new StackFrame(data));
    }

    private String resolvedAttribute(final EList<String> params, final int i) {
        // TODO: resolve from vars/map/etc
        return params.size() < i ? null : params.get(i - 1);
    }

    private static class StackFrame {

        private final NamedValuePipe headPipe;

        private NamedValuePipe pipe;
        private boolean inEntityName;
        private boolean inCondition;

        private StackFrame(final NamedValuePipe headPipe) {
            this.headPipe = headPipe;
            this.pipe = headPipe;
        }

        public NamedValuePipe getHeadPipe() {
            return headPipe;
        }

        public void setPipe(final NamedValuePipe pipe) {
            this.pipe = pipe;
        }

        public NamedValuePipe getPipe() {
            return pipe;
        }

        public void setInEntityName(final boolean inEntityName) {
            this.inEntityName = inEntityName;
        }

        public boolean isInEntityName() {
            return inEntityName;
        }

        public void setInCondition(final boolean inCondition) {
            this.inCondition = inCondition;
        }

        public boolean isInCondition() {
            return inCondition;
        }

    }

}
