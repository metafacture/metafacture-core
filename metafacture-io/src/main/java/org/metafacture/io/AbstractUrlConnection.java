/*
 * Copyright 2022 hbz
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

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Common functions for UrlConnections.
 *
 * @param <T> object type that this module processes
 * @param <R> receiver type of the downstream module
 * @author Jens Wille
 * @author Pascal Christoph (dr0i)
 */
abstract class AbstractUrlConnection<T, R> extends DefaultObjectPipe<T, ObjectReceiver<R>> {
    public static final String ACCEPT_HEADER = "accept";
    public static final String ENCODING_HEADER = "accept-charset";
    public static final String CONTENT_TYPE_HEADER = "content-Type";
    static final String ACCEPT_DEFAULT = "*/*";
    static final String ENCODING_DEFAULT = "UTF-8";
    private static Boolean doOutput = false;
    private final Map<String, String> headers = new HashMap<>();
    private final Pattern headerFieldSeparator = Pattern.compile("\n");
    private final Pattern headerValueSeparator = Pattern.compile(":");
    private URLConnection con;

    /**
     * Sets the HTTP {@value #ACCEPT_HEADER} header. This is a mime-type such as text/plain
     * or text/html.
     *
     * @param accept mime-type to use for the HTTP accept header
     */
    public void setAccept(final String accept) {
        setHeader(ACCEPT_HEADER, accept);
    }

    /**
     * Sets the preferred encoding of the HTTP response. This value is set as the
     * {@value #ENCODING_HEADER} header. Additionally, the encoding is used for reading the
     * HTTP response if it does not specify an encoding.
     *
     * @param encoding name of the encoding used for the accept-charset HTTP
     *                 header
     */
    public void setEncoding(final String encoding) {
        setHeader(ENCODING_HEADER, encoding);
    }

    /**
     * Sets the HTTP {@value #CONTENT_TYPE_HEADER} header. This is a mime-type such as text/plain,
     * text/html or application/x-ndjson.
     *
     * @param contentType mime-type to use for the HTTP contentType header
     */
    public void setContentType(final String contentType) {
        setHeader(CONTENT_TYPE_HEADER, contentType);
    }

    /**
     * Set the DoOutput flag to true if you intend to use the URL connection for output, false if not. The default is false.
     *
     * @param doOutput the new value
     * @see URLConnection#setDoOutput(boolean)
     */
    public void setDoOutput(final boolean doOutput) {
        AbstractUrlConnection.doOutput = doOutput;
    }

    /**
     * Gets the {@value #ENCODING_HEADER} header of the URLConnection.
     *
     * @return the name of the encoding header
     */
    public String getEncodingHeader() {
        return ENCODING_HEADER;
    }

    /**
     * Sets a request property, or multiple request properties separated by
     * {@code \n}.
     *
     * @param header request property line
     */
    public void setHeader(final String header) {
        Arrays.stream(headerFieldSeparator.split(header)).forEach(h -> {
            final String[] parts = headerValueSeparator.split(h, 2);
            if (parts.length == 2) {
                setHeader(parts[0], parts[1].trim());
            }
            else {
                throw new IllegalArgumentException("Invalid header: " + h);
            }
        });
    }

    /**
     * Sets a request property.
     *
     * @param key   request property key
     * @param value request property value
     */
    public void setHeader(final String key, final String value) {
        headers.put(key.toLowerCase(), value);
    }

    /**
     * Gets headers of the {@link java.net.URLConnection} as a HashMap.
     *
     * @return the headers od the URLConnection as HashMap
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Opens an {@link java.net.URLConnection} defined by the URL.
     *
     * @param urlStr an URL as String
     * @return {@link java.net.URLConnection}
     * @throws IOException if an I/O IOException occurs
     */
    public URLConnection getUrlConnection(final String urlStr) throws IOException {
        final URL url = new URL(urlStr);
        con = url.openConnection();
        con.setDoOutput(doOutput);
        headers.forEach(con::addRequestProperty);
        return con;
    }

}
