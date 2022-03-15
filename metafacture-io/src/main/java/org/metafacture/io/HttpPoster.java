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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Uploads data using {@link URLConnection} with POST method and passes the response to the receiver.
 * Supports the setting of 'Accept', 'ContentType' and 'Encoding' as HTTP header fields.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("POSTs data to a {@link URLConnection}. Supports the setting of 'accept', 'contentType' and encoding as http header fields. Use 'url' to set URL.")
@In(String.class)
@Out(String.class)
@FluxCommand("post-http")
public final class HttpPoster extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String POST = "POST";
    private String encoding = "UTF-8";
    private String contentType = "application/json";
    private URL url;
    private String accept = "*/*";

    /**
     * Creates an instance of {@link HttpPoster}.
     *
     * @throws ProtocolException if a protocol error occurs
     */
    public HttpPoster() throws ProtocolException {
    }

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
     * Sets the HTTP contentType header. This is a mime-type such as text/plain,
     * text/html or application/x-ndjson. The default value is "application/x-ndjson".
     *
     * @param contentType mime-type to use for the HTTP contentType header
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the preferred encoding of the HTTP response. This value is in the
     * accept-charset header. Additonally, the encoding is used for reading the
     * HTTP resonse if it does not  specify an encoding. The default value for
     * the encoding is UTF-8.
     *
     * @param encoding name of the encoding used for the contentType-charset HTTP
     *                 header
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the HTTP URL to POST to
     *
     * @param url the URL to post to
     * @throws MalformedURLException if an URL is malformed
     */
    public void setUrl(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    @Override
    public void process(final String data) throws IllegalStateException, NullPointerException {

        //    String encodedData = URLEncoder.encode( data, this.encoding );
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) this.url.openConnection();
            BufferedReader br = null;
            conn.setDoOutput(true);
            conn.setRequestMethod(POST);
            conn.setRequestProperty("Accept", this.accept);
            conn.setRequestProperty(CONTENT_TYPE, this.contentType);
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            if (100 <= conn.getResponseCode() && conn.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String responseBody = br.lines().collect(Collectors.joining());
//            getReceiver().process(conn.getResponseMessage());
            getReceiver().process(responseBody);


        } catch (IOException e) {
            throw new MetafactureException(e);
        }

    }
}
