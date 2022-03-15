/*
 * Copyright 2013 - 2022 Deutsche Nationalbibliothek et al.
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

package org.metafacture.io;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLConnection;

/**
 * Opens a {@link java.net.URLConnection} and passes a reader to the receiver.
 *
 * @author Christoph Böhme
 * @author Jan Schnasse
 */
@Description("Opens an HTTP resource. Supports the setting of `Accept` and `Accept-Charset` as HTTP header fields, as well as generic headers (separated by `\\n`).")
@In(String.class)
@Out(Reader.class)
@FluxCommand("open-http")
public final class HttpOpener extends AbstractUrlConnection<String, Reader> {

    /**
     * Creates an instance of {@link HttpOpener}.
     * Sets the default value for the {@value #ENCODING_HEADER} is {@value #ENCODING_DEFAULT}.
     * The default value of the {@value #ACCEPT_HEADER} is *&#47;* which mean any mime-type.
     */
    public HttpOpener() {
        setAccept(ACCEPT_DEFAULT);
        setEncoding(ENCODING_DEFAULT);
    }

    @Override
    public void process(final String urlStr) {
        try {
            final URLConnection con = getUrlConnection(urlStr);
            String enc = con.getContentEncoding();
            if (enc == null) {
                enc = getHeaders().get(ENCODING_HEADER);
            }
            getReceiver().process(new InputStreamReader(con.getInputStream(), enc));
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

}
