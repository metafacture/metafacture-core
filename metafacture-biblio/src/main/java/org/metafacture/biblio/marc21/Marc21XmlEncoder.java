/*
 *  Copyright 2024 hbz
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.metafacture.biblio.marc21;


import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;


/**
 * Acts as a wrapper: pipes input to Marc21Encoder which output is piped to Marc21Decoder which output is piped to MarcXmlHandler.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@In(StreamReceiver.class)
@Out(String.class)
@Description("Encodes MARC21 records as MARCXML. Similar to encode-marcxml but safer especially when the 'leader' has to be computed.")
@FluxCommand("encode-marc21xml")
public class Marc21XmlEncoder implements StreamPipe<ObjectReceiver<String>> {
    private final Marc21Decoder marc21Decoder = new Marc21Decoder();
    private final MarcXmlEncoder marcXmlEncoder= new MarcXmlEncoder();
    private final  Marc21Encoder marc21Encoder = new Marc21Encoder();

    public Marc21XmlEncoder() {
        marc21Decoder.setEmitLeaderAsWhole(true);

        marc21Encoder.setReceiver(marc21Decoder);
        marc21Decoder.setReceiver(marcXmlEncoder);
    }

    @Override
    public <R extends ObjectReceiver<String>> R setReceiver(R receiver) {
        this.marcXmlEncoder.setReceiver(receiver);
        return receiver;
    }

    @Override
    public void resetStream() {
        marc21Encoder.resetStream();

    }

    @Override
    public void closeStream() {
        marc21Encoder.closeStream();

    }

    @Override
    public void startRecord(final String identifier) {
        marc21Encoder.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        marc21Encoder.endRecord();

    }

    @Override
    public void startEntity(final String name) {
        marc21Encoder.startEntity(name);

    }

    @Override
    public void endEntity() {
        marc21Encoder.endEntity();

    }

    @Override
    public void literal(final String name, final String value) {
        marc21Encoder.literal(name, value);

    }
}

