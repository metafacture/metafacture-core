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
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

/**
 * Acts as a wrapper: pipes input to Marc21Encoder which output is piped to Marc21Decoder which output is piped to MarcXmlEncoder.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@In(StreamReceiver.class)
@Out(String.class)
@Description("Encodes MARC21 records as MARCXML. It wraps 'encode-marc21 | decode-marc21 | encode-marcxml ' to generate MARCXML more safely, especially when the building the 'leader'.")
@FluxCommand("encode-marc21xml")
public class Marc21XmlEncoder extends MarcXmlEncoderAbstract {
    private final Marc21Decoder marc21Decoder = new Marc21Decoder();
    private final Marc21Encoder marc21Encoder = new Marc21Encoder ();
    private final MarcXmlEncoder marcXmlEncoder =new MarcXmlEncoder();
    public Marc21XmlEncoder() {
        marc21Decoder.setEmitLeaderAsWhole(true);

        marc21Encoder.setReceiver(marc21Decoder);
        marc21Decoder.setReceiver(marcXmlEncoder);
    }
    @Override
    protected void onSetReceiver() {
        marcXmlEncoder.setReceiver(getReceiver());
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

    @Override
    protected void onCloseStream() {
        marc21Encoder.closeStream();
    }

    @Override
    public void onResetStream() {
        marc21Encoder.resetStream();
    }

    @Override
    public void setEmitNamespace(boolean emitNamespace) {
        marcXmlEncoder.setEmitNamespace(emitNamespace);
    }

    @Override
    public void omitXmlDeclaration(boolean currentOmitXmlDeclaration) {
        marcXmlEncoder.omitXmlDeclaration(currentOmitXmlDeclaration);
    }

    @Override
    public void setXmlVersion(String xmlVersion) {
        marcXmlEncoder.setXmlVersion(xmlVersion);
    }

    @Override
    public void setXmlEncoding(String xmlEncoding) {
        marcXmlEncoder.setXmlEncoding(xmlEncoding);
    }

    @Override
    public void setFormatted(boolean formatted) {
        marcXmlEncoder.setFormatted(formatted);
    }
}

