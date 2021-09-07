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
package org.metafacture.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;


/**
 * Opens a {@link URLConnection} and passes a reader to the receiver.
 *
 * @author Christoph Böhme
 * @author Jan Schnasse
 */
@Description("Opens a http resource. Supports the setting of Accept and Accept-Charset as http header fields.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-http")
public final class HttpOpener
        extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private String encoding = "UTF-8";
    private String accept = "*/*";
    private String auth = null;

    /**
     * Sets the HTTP accept header value. This is a mime-type such as text/plain
     * or text/html. The default value of the accept is *&#47;* which means
     * any mime-type.
     *
     * @param accept mime-type to use for the HTTP accept header
     */
    public void setAccept(final String accept) {
        this.accept = accept;
    }

    /**
     * Sets the preferred encoding of the HTTP response. This value is in the
     * accept-charset header. Additonally, the encoding is used for reading the
     * HTTP resonse if it does not  specify an encoding. The default value for
     * the encoding is UTF-8.
     *
     * @param encoding name of the encoding used for the accept-charset HTTP
     *                 header
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the basic authentication credentials
     *
     * @param auth The <user:password> string
     */
    public void setAuth(final String auth) {
        this.auth = auth;
    }

    @Override
    public void process(final String urlStr) {
        try {
            final URL url = new URL(urlStr);
            final URLConnection con = url.openConnection();
            con.addRequestProperty("Accept", accept);
            con.addRequestProperty("Accept-Charset", encoding);
            if (auth != null) {
                final byte[] base64EncodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                con.addRequestProperty("Authorization", "Basic " + new String(base64EncodedAuth));
            }
            String enc = con.getContentEncoding();
            if (enc == null) {
                enc = encoding;
            }
            getReceiver().process(new InputStreamReader(con.getInputStream(), enc));
        } catch (IOException e) {
            throw new MetafactureException(e);
        }
    }
}
