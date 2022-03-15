/*
 * Copyright 2022 Pascal Christoph, hbz
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
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * Uploads data using {@link java.net.HttpURLConnection} with POST method and passes the response to the receiver.
 * Supports the setting of 'Accept', 'ContentType' and 'Encoding' as HTTP header fields.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("POSTs data to a {@link java.net.HttpURLConnection}. Argument 'url' is mandatory. Supports the setting of 'accept', 'contentType' and 'encoding' (of the response) as http header fields.")
@In(String.class)
@Out(Reader.class)
@FluxCommand("post-http")
public final class HttpPoster extends AbstractUrlConnection<String, Reader> {

    private static final String POST = "POST";
    private static final Boolean DO_OUTPUT = true;
    private static final int HTTP_STATUS_CODE_MIN = 100;
    private static final int HTTP_STATUS_CODE_MAX = 399;
    private String contentType = "application/json";
    private String url;

    /**
     * Creates an instance of {@link HttpPoster}.
     *
     * @throws ProtocolException if a protocol error occurs
     */
    public HttpPoster() throws ProtocolException {
        setDoOutput(DO_OUTPUT);
    }

    /**
     * Sets the HTTP URL to POST to
     *
     * @param url the URL to POST to
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public void process(final String data) throws IllegalStateException, NullPointerException {
        try {
            final HttpURLConnection conn = (HttpURLConnection) getUrlConnection(url);
            conn.setRequestMethod(POST);
            final InputStreamReader inputStreamReader;
            final OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            if (HTTP_STATUS_CODE_MIN <= conn.getResponseCode() && conn.getResponseCode() <= HTTP_STATUS_CODE_MAX) {
                inputStreamReader = new InputStreamReader(conn.getInputStream());
            }
            else {
                inputStreamReader = new InputStreamReader(conn.getErrorStream());
            }
            getReceiver().process(inputStreamReader);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }
}
