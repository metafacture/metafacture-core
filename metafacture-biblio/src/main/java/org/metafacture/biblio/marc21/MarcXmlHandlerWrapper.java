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
import org.metafacture.framework.helpers.DefaultStreamPipe;


/**
 * Acts as a wrapper: pipes input to Marc21Encoder which output is piped to Marc21Decoder which output is piped to MarcXmlHandler.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@In(StreamReceiver.class)
@Out(String.class)
@Description("Encodes MARC21 records as MARCXML. Similar to encode-marcxml but safer e.g. when the 'leader' has to be computed.")
@FluxCommand("encode-marc21xml")
public class MarcXmlHandlerWrapper extends DefaultStreamPipe<ObjectReceiver<String>> {
    private final Marc21Decoder marc21Decoder = new Marc21Decoder();
    private final MarcXmlEncoder marcXmlEncoder = new MarcXmlEncoder();

    public MarcXmlHandlerWrapper() {
        this(new Marc21Encoder());

	}
    public MarcXmlHandlerWrapper(StreamPipe<ObjectReceiver<String>> encoder) {
        marc21Decoder.setEmitLeaderAsWhole(true);
        marc21Decoder.setReceiver(marcXmlEncoder);
        this.setReceiver(marc21Decoder);
    }


}
