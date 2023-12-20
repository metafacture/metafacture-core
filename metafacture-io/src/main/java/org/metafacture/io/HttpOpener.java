/*
 * Copyright 2013, 2023 Deutsche Nationalbibliothek et al
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
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Opens an {@link HttpURLConnection} and passes a reader to the receiver.
 *
 * @author Christoph Böhme
 * @author Jan Schnasse
 * @author Jens Wille
 * @author Pascal Christoph (dr0i)
 */
@Description("Opens an HTTP resource. Supports setting HTTP header fields `Accept`, `Accept-Charset`, `Accept-Encoding`, `Content-Encoding` and `Content-Type`, as well as generic headers (separated by `\\n`). Defaults: request `method` = `GET`, request `url` = `@-` (input data), request `body` = `@-` (input data) if request method supports body and input data not already used, `Accept` header (`accept`) = `*/*`, `Accept-Charset` header (`acceptcharset`) = `UTF-8`, `errorprefix` = `ERROR: `.")
@In(String.class)
@Out(Reader.class)
@FluxCommand("open-http")
public final class HttpOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    public static final String ACCEPT_HEADER = "accept";
    public static final String ACCEPT_CHARSET_HEADER = "accept-charset";
    public static final String ACCEPT_ENCODING_HEADER = "accept-encoding";
    public static final String CONTENT_ENCODING_HEADER = "content-encoding";
    public static final String CONTENT_TYPE_HEADER = "content-type";

    public static final String ACCEPT_DEFAULT = "*/*";
    public static final String CHARSET_DEFAULT = "UTF-8";
    public static final String DEFAULT_PREFIX = "ERROR: ";
    public static final String HEADER_FIELD_SEPARATOR = "\n";
    public static final String HEADER_VALUE_SEPARATOR = ":";
    public static final String INPUT_DESIGNATOR = "@-";
    public static final String MIME_PARAMETER_CHARSET = "charset";
    public static final String MIME_PARAMETER_SEPARATOR = ";";
    public static final String MIME_PARAMETER_VALUE_SEPARATOR = "=";

    public static final String DEFAULT_METHOD_NAME = "GET";
    public static final Method DEFAULT_METHOD = Method.valueOf(DEFAULT_METHOD_NAME);

    private static final Pattern HEADER_FIELD_SEPARATOR_PATTERN = Pattern.compile(HEADER_FIELD_SEPARATOR);
    private static final Pattern HEADER_VALUE_SEPARATOR_PATTERN = Pattern.compile(HEADER_VALUE_SEPARATOR);
    private static final Pattern MIME_PARAMETER_SEPARATOR_PATTERN = Pattern.compile(MIME_PARAMETER_SEPARATOR);

    private static final int ALLOWED_REDIRECTIONS = 3;
    private static final int CONNECTION_TIMEOUT = 11000;

    private final Map<String, String> headers = new HashMap<>();

    private Method method;
    private String body;
    private String errorPrefix;
    private String url;
    private boolean inputUsed;

    public enum Method {

        DELETE(false, true),
        GET(false, true),
        HEAD(false, false),
        OPTIONS(false, true),
        POST(true, true),
        PUT(true, true),
        TRACE(false, true);

        private final boolean requestHasBody;
        private final boolean responseHasBody;

        Method(final boolean requestHasBody, final boolean responseHasBody) {
            this.requestHasBody = requestHasBody;
            this.responseHasBody = responseHasBody;
        }

        /**
         * Checks whether the request method accepts a request body.
         *
         * @return true if the request method accepts a request body
         */
        public boolean getRequestHasBody() {
            return requestHasBody;
        }

        /**
         * Checks whether the request method returns a response body.
         *
         * @return true if the request method returns a response body
         */
        public boolean getResponseHasBody() {
            return responseHasBody;
        }

    }

    /**
     * Creates an instance of {@link HttpOpener}.
     */
    public HttpOpener() {
        setAccept(ACCEPT_DEFAULT);
        setAcceptCharset(CHARSET_DEFAULT);
        setErrorPrefix(DEFAULT_PREFIX);
        setMethod(DEFAULT_METHOD);
        setUrl(INPUT_DESIGNATOR);
    }

    /**
     * Sets the HTTP {@value ACCEPT_HEADER} header value. This is a MIME type
     * such as {@code text/plain} or {@code application/json}. The default
     * value for the accept header is {@value ACCEPT_DEFAULT} which means
     * any MIME type.
     *
     * @param accept MIME type to use for the HTTP accept header
     */
    public void setAccept(final String accept) {
        setHeader(ACCEPT_HEADER, accept);
    }

    /**
     * Sets the HTTP request body. The default value for the request body is
     * {@value INPUT_DESIGNATOR} <i>if the {@link #setMethod(Method) request
     * method} accepts a request body</i>, which means it will use the {@link
     * #process(String) input data} data as request body <i>if the input has
     * not already been used</i>; otherwise, no request body will be set by
     * default.
     *
     * <p>If a request body has been set, but the request method does not
     * accept a body, the method <i>may</i> be changed to {@code POST}.
     *
     * @param body the request body
     */
    public void setBody(final String body) {
        this.body = body;
    }

    /**
     * Sets the HTTP {@value CONTENT_TYPE_HEADER} header value. This is a
     * MIME type such as {@code text/plain} or {@code application/json}.
     *
     * @param contentType MIME type to use for the HTTP content-type header
     */
    public void setContentType(final String contentType) {
        setHeader(CONTENT_TYPE_HEADER, contentType);
    }

    /**
     * Sets the HTTP {@value ACCEPT_CHARSET_HEADER} header value. This is the
     * preferred charset for the HTTP response.
     * The default charset is {@value CHARSET_DEFAULT}.
     *
     * @param charset name of the charset used for the accept-charset HTTP header
     */
    public void setAcceptCharset(final String charset) {
        setHeader(ACCEPT_CHARSET_HEADER, charset);
    }

    /**
     * @deprecated Use {@link #setAcceptCharset} instead.
     * @param charset name of the charset used for the accept-charset HTTP header
     */
    @Deprecated
    public void setEncoding(final String charset) {
        setAcceptCharset(charset);
    }

    /**
     * Sets the HTTP {@value ACCEPT_ENCODING_HEADER} header value. This is the
     * preferred content encoding for the HTTP response. It accepts HTTP compression.
     * Allowed values are i.a. "gzip" and "Brotli".
     * The default for the content encoding is null, which means "no compression".
     *
     * @param acceptEncoding name of content encoding used for the accept-encoding HTTP
     *                 header
     */
    public void setAcceptEncoding(final String acceptEncoding) {
        setHeader(ACCEPT_ENCODING_HEADER, acceptEncoding);
    }

    /**
     * Sets the HTTP {@value CONTENT_ENCODING_HEADER} header value. This is the
     * content encoding for the HTTP request. It enables HTTP compression.
     * Allowed values are "gzip".
     * The default for the content encoding is null, which means "no compression".
     *
     * @param contentEncoding name of content encoding used for the content-encoding HTTP
     *                 header
     */
    public void setContentEncoding(final String contentEncoding) {
        setHeader(CONTENT_ENCODING_HEADER, contentEncoding);
    }

    /**
     * Sets the error prefix. The default error prefix is
     * {@value DEFAULT_PREFIX}.
     *
     * @param errorPrefix the error prefix
     */
    public void setErrorPrefix(final String errorPrefix) {
        this.errorPrefix = errorPrefix;
    }

    /**
     * Sets a request property (header), or multiple request properties
     * separated by {@value HEADER_FIELD_SEPARATOR}. Header name and value
     * are separated by {@value HEADER_VALUE_SEPARATOR}. The header name is
     * case-insensitive.
     *
     * @param header request property line
     *
     * @see #setHeader(String, String)
     */
    public void setHeader(final String header) {
        Arrays.stream(HEADER_FIELD_SEPARATOR_PATTERN.split(header)).forEach(h -> {
            final String[] parts = HEADER_VALUE_SEPARATOR_PATTERN.split(h, 2);
            if (parts.length == 2) {
                setHeader(parts[0], parts[1].trim());
            }
            else {
                throw new IllegalArgumentException("Invalid header: " + h);
            }
        });
    }

    /**
     * Sets a request property (header). The header name is case-insensitive.
     *
     * @param key request property key
     * @param value request property value
     */
    public void setHeader(final String key, final String value) {
        headers.put(key.toLowerCase(), value);
    }

    /**
     * Sets the HTTP request method. The default request method is
     * {@value DEFAULT_METHOD_NAME}.
     *
     * @param method the request method
     */
    public void setMethod(final Method method) {
        this.method = method;
    }

    /**
     * Sets the HTTP request URL. The default value for the request URL is
     * {@value INPUT_DESIGNATOR}, which means it will use the {@link
     * #process(String) input data} as request URL.
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
                body == null && method.getRequestHasBody() ? INPUT_DESIGNATOR : body);
            Reader reader = null;
            if (requestBody != null) {
                reader = doPostOrPut(requestBody, new URL(requestUrl));
            }
            else {
                reader = doGet(requestUrl);
            }
            getReceiver().process(reader);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
        finally {
            inputUsed = false;
        }
    }

    private Reader doPostOrPut(final String requestBody, final URL urlToOpen) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) urlToOpen.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod(method.name());
        headers.forEach(connection::setRequestProperty);
        connection.getOutputStream().write(requestBody.getBytes());
        final InputStream inputStream = getInputStream(connection);
        return new InputStreamReader(inputStream, getContentCharset(connection));
    }

    private Reader doGet(final String requestUrl) throws IOException {
        final Reader reader;
        final HttpURLConnection connection;
        connection = followRedirects(new URL(requestUrl));
        final InputStream inputStream = getInputStream(connection);

        if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
            final GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            reader = new InputStreamReader(gzipInputStream, getContentCharset(connection));
        }
        else {
            reader = new InputStreamReader(inputStream, getContentCharset(connection));
        }
        return reader;
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

    private InputStream getInputStream(final HttpURLConnection connection) throws IOException {
        try {
            return connection.getInputStream();
        }
        catch (final IOException e) {
            final InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                return getErrorStream(errorStream);
            }
            else {
                throw e;
            }
        }
    }

    private InputStream getErrorStream(final InputStream errorStream) {
        if (errorPrefix != null) {
            final InputStream errorPrefixStream = new ByteArrayInputStream(errorPrefix.getBytes());
            return new SequenceInputStream(errorPrefixStream, errorStream);
        }
        else {
            return errorStream;
        }
    }

    private String getContentCharset(final HttpURLConnection connection) {
        final String contentType = connection.getContentType();

        if (contentType != null) {
            final String[] parts = MIME_PARAMETER_SEPARATOR_PATTERN.split(contentType);

            for (int i = 1; i < parts.length; ++i) {
                final String parameter = parts[i].trim();
                final int index = parameter.indexOf(MIME_PARAMETER_VALUE_SEPARATOR);

                if (index != -1 && MIME_PARAMETER_CHARSET.equalsIgnoreCase(parameter.substring(0, index))) {
                    return parameter.substring(index + 1);
                }
            }
        }

        return CHARSET_DEFAULT;
    }

    private HttpURLConnection followRedirects(final URL startingUrl) throws IOException {
        int times = 0;
        HttpURLConnection conn;
        URL urlToFollow = startingUrl;
        while (true) {
            times = times + 1;

            if (times > ALLOWED_REDIRECTIONS) {
                throw new IOException("Stuck in redirect loop");
            }

            conn = (HttpURLConnection) urlToFollow.openConnection();
            headers.forEach(conn::setRequestProperty);
            conn.setRequestMethod(method.name());
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setInstanceFollowRedirects(false); // Make the logic below easier to detect redirections

            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    String location = conn.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    urlToFollow = new URL(urlToFollow, location); // Deal with relative URLs
                    continue;
                default:
                    break;
            }
            break;
        }
        return conn;
    }

}
