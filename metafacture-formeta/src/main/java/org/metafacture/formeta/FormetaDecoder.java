/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.formeta;

import org.metafacture.formeta.parser.Emitter;
import org.metafacture.formeta.parser.FormetaParser;
import org.metafacture.formeta.parser.FullRecordEmitter;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Decodes a record in formeta format.
 *
 * @author Christoph Böhme
 *
 */
@Description("Decodes a record in formeta format.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-formeta")
public final class FormetaDecoder extends
        DefaultObjectPipe<String, StreamReceiver> {

    private final FormetaParser parser = new FormetaParser();
    private final Emitter emitter = new FullRecordEmitter();

    /**
     * Creates an instance of {@link FormetaDecoder}.
     */
    public FormetaDecoder() {
        parser.setEmitter(emitter);
    }

    @Override
    public void process(final String record) {
        parser.parse(record);
    }

    @Override
    protected void onSetReceiver() {
        emitter.setReceiver(getReceiver());
    }

}
