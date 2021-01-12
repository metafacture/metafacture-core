/*
 * Copyright 2013, 2021 Deutsche Nationalbibliothek and others
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

import org.metafacture.flowcontrol.StreamBuffer;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.javaintegration.SingleValue;
import org.metafacture.metamorph.api.InterceptorFactory;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Filters a stream based on a fix definition. A record is accepted if the
 * morph returns at least one non empty value.
 *
 * @author Markus Michael Geipel
 * @author Fabian Steeg, hbz (adapted for Metafix)
 *
 */
@Description("Filters a stream based on a fix definition. A record is accepted if the fix returns at least one non empty value.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("fix-filter")
public final class MetafixFilter extends DefaultStreamPipe<StreamReceiver> {

    private final StreamBuffer buffer = new StreamBuffer();
    private final SingleValue singleValue = new SingleValue();
    private final Metafix metafix;

    public MetafixFilter(final String morphDef) throws FileNotFoundException {
        metafix = new Metafix(morphDef);
        metafix.setReceiver(singleValue);
    }

    public MetafixFilter(final Metafix metamorph) {
        this.metafix = metamorph;
        metamorph.setReceiver(singleValue);
    }

    public MetafixFilter(final String morphDef, final Map<String, String> vars) throws FileNotFoundException {
        metafix = new Metafix(morphDef, vars);
        metafix.setReceiver(singleValue);
    }

    public MetafixFilter(final String morphDef, final InterceptorFactory interceptorFactory) throws FileNotFoundException {
        metafix = new Metafix(morphDef, interceptorFactory);
        metafix.setReceiver(singleValue);
    }

    @Override
    protected void onSetReceiver() {
        buffer.setReceiver(getReceiver());
    }

    private void dispatch() {
        final String key = singleValue.getValue();
        if (!key.isEmpty()) {
            buffer.replay();
        }
        buffer.clear();
    }

    @Override
    public void startRecord(final String identifier) {
        buffer.startRecord(identifier);
        metafix.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        buffer.endRecord();
        metafix.endRecord();
        dispatch();
    }

    @Override
    public void startEntity(final String name) {
        buffer.startEntity(name);
        metafix.startEntity(name);
    }

    @Override
    public void endEntity() {
        buffer.endEntity();
        metafix.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        buffer.literal(name, value);
        metafix.literal(name, value);
    }

    @Override
    protected void onResetStream() {
        buffer.clear();
        metafix.resetStream();
    }

    @Override
    protected void onCloseStream() {
        buffer.clear();
        metafix.closeStream();
    }
}
