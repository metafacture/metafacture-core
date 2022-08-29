/*
 * Copyright 2013, 2022 Deutsche Nationalbibliothek et al
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Opens an {@link HttpURLConnection} and passes a reader to the receiver.
 *
 * @author Christoph Böhme
 * @author Jan Schnasse
 */
@Description("Opens an HTTP resource. Supports the setting of `Accept` and `Accept-Charset` as HTTP header fields, as well as generic headers (separated by `\\n`).")
@In(String.class)
@Out(Reader.class)
@FluxCommand("open-http")
public final class HttpOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private static final Pattern HEADER_FIELD_SEPARATOR = Pattern.compile("\n");
    private static final Pattern HEADER_VALUE_SEPARATOR = Pattern.compile(":");

    private static final String ACCEPT_DEFAULT = "*/*";
    private static final String ACCEPT_HEADER = "accept";
    private static final String CONTENT_TYPE_HEADER = "content-type";
    private static final String DEFAULT_PREFIX = "ERROR: ";
    private static final String ENCODING_DEFAULT = "UTF-8";
    private static final String ENCODING_HEADER = "accept-charset";
    private static final String INPUT_DESIGNATOR = "@-";

    private static final Method DEFAULT_METHOD = Method.GET;

    private final Map<String, String> headers = new HashMap<>();

    private Method method;
    private String body;
    private String errorPrefix;
    private String url;
    private boolean inputUsed;

    public enum Method {

        DELETE(false),
        GET(false),
        HEAD(false),
        OPTIONS(false),
        POST(true),
        PUT(true),
        TRACE(false);

        private final boolean inputAsBody;

        Method(final boolean inputAsBody) {
            this.inputAsBody = inputAsBody;
        }

        private boolean getInputAsBody() {
            return inputAsBody;
        }

    }

    /**
     * Creates an instance of {@link HttpOpener}.
     */
    public HttpOpener() {
        setAccept(ACCEPT_DEFAULT);
        setContentType(ACCEPT_DEFAULT);
        setEncoding(ENCODING_DEFAULT);
        setErrorPrefix(DEFAULT_PREFIX);
        setMethod(DEFAULT_METHOD);
        setUrl(INPUT_DESIGNATOR);
    }

    /**
     * Sets the HTTP accept header value. This is a mime-type such as text/plain
     * or text/html. The default value of the accept is *&#47;* which means
     * any mime-type.
     *
     * @param accept mime-type to use for the HTTP accept header
     */
    public void setAccept(final String accept) {
        setHeader(ACCEPT_HEADER, accept);
    }

    /**
     * Sets the HTTP request body.
     *
     * @param body the request body
     */
    public void setBody(final String body) {
        this.body = body;
    }

    /**
     * Sets the HTTP content type header. This is a mime-type such as text/plain,
     * text/html. The default is application/json.
     *
     * @param contentType mime-type to use for the HTTP contentType header
     */
    public void setContentType(final String contentType) {
        setHeader(CONTENT_TYPE_HEADER, contentType);
    }

    /**
     * Sets the preferred encoding of the HTTP response. This value is in the
     * accept-charset header. Additonally, the encoding is used for reading the
     * HTTP resonse if it does not specify an encoding. The default value for
     * the encoding is UTF-8.
     *
     * @param encoding name of the encoding used for the accept-charset HTTP
     *                 header
     */
    public void setEncoding(final String encoding) {
        setHeader(ENCODING_HEADER, encoding);
    }

    /**
     * Sets the error prefix.
     *
     * @param errorPrefix the error prefix
     */
    public void setErrorPrefix(final String errorPrefix) {
        this.errorPrefix = errorPrefix;
    }

    /**
     * Sets a request property, or multiple request properties separated by
     * {@code \n}.
     *
     * @param header request property line
     */
    public void setHeader(final String header) {
        Arrays.stream(HEADER_FIELD_SEPARATOR.split(header)).forEach(h -> {
            final String[] parts = HEADER_VALUE_SEPARATOR.split(h, 2);
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
     * @param key request property key
     * @param value request property value
     */
    public void setHeader(final String key, final String value) {
        headers.put(key.toLowerCase(), value);
    }

    /**
     * Sets the HTTP request method.
     *
     * @param method the request method
     */
    public void setMethod(final Method method) {
        this.method = method;
    }

    /**
     * Sets the HTTP request URL.
     *
     * @param url the request URL
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public void process(final String input) {
        try {
            final String requestUrl = getInput(input, url);
            final String requestBody = getInput(input,
                    body == null && method.getInputAsBody() ? INPUT_DESIGNATOR : body);

            final HttpURLConnection connection =
                (HttpURLConnection) new URL(requestUrl).openConnection();

            connection.setRequestMethod(method.name());
            headers.forEach(connection::addRequestProperty);

            if (requestBody != null) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(requestBody.getBytes());
            }

            final InputStream errorStream = connection.getErrorStream();
            final InputStream inputStream;

            if (errorStream != null) {
                if (errorPrefix != null) {
                    final InputStream errorPrefixStream = new ByteArrayInputStream(errorPrefix.getBytes());
                    inputStream = new SequenceInputStream(errorPrefixStream, errorStream);
                }
                else {
                    inputStream = errorStream;
                }
            }
            else {
                inputStream = connection.getInputStream();
            }

            final String contentEncoding = getEncoding(connection.getContentEncoding());
            getReceiver().process(new InputStreamReader(inputStream, contentEncoding));
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private String getInput(final String input, final String value) {
        final String result;

        if (!INPUT_DESIGNATOR.equals(value)) {
            result = value;
        }
        else if (inputUsed) {
            result = null;
        }
        else {
            inputUsed = true;
            result = input;
        }

        return result;
    }

    private String getEncoding(final String contentEncoding) {
        return contentEncoding != null ? contentEncoding : headers.get(ENCODING_HEADER);
    }

}
