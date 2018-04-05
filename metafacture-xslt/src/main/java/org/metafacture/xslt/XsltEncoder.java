/*
 * Copyright 2018 Deutsche Nationalbibliothek
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
package org.metafacture.xslt;

import net.sf.saxon.s9api.Destination;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.xslt.adapter.OutputStreamToObjectReceiver;

@In(XmlReceiver.class)
@Out(ObjectReceiver.class)
@Description("Transforms generic XML (SAX event stream) by applying a XSLT using a custom stylesheet.")
@FluxCommand("encode-xslt")
public class XsltEncoder extends DefaultXsltPipe<ObjectReceiver<String>>
{
    public XsltEncoder(String stylesheetId)
    {
        super(stylesheetId);
    }

    Destination getDestination() {
        return getProcessor().newSerializer(new OutputStreamToObjectReceiver(getReceiver()));
    }
}
