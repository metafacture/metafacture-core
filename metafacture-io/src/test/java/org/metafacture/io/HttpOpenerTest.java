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

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.*;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

import static org.mockito.Mockito.times;

/**
 * Tests for class {@link HttpOpener}.
 *
 * @author Jens Wille
 */
public final class HttpOpenerTest {

    private static final String TEST_PATH = "/test/path";
    private static final String TEST_URL = "%s" + TEST_PATH;

    private static final String TEST_STRING = "test string";
    private static final StringValuePattern TEST_VALUE = WireMock.equalTo(TEST_STRING);

    private static final String REQUEST_BODY = "request body";
    private static final String RESPONSE_BODY = "response bödy"; // UTF-8
    private static byte[] GZIPPED_RESPONSE_BODY;
    static {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(RESPONSE_BODY.getBytes("UTF-8"));
            gzip.close();
            GZIPPED_RESPONSE_BODY = out.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .jettyAcceptors(Runtime.getRuntime().availableProcessors())
            .dynamicPort());

    @Mock
    private ObjectReceiver<Reader> receiver;

    @Captor
    private ArgumentCaptor<Reader> processedObject;

    @Test
    public void shouldPerformGetRequestWithInputAsUrlByDefault() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {});
    }

    @Test
    public void shouldPerformGetRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(TEST_STRING, HttpOpener.Method.GET, (o, u) -> {
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformPostRequestWithInputAsUrl() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.POST, (o, u) -> {
            o.setMethod(HttpOpener.Method.POST);
            o.setBody(REQUEST_BODY);
        });
    }

    @Test
    public void shouldPerformPostRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.POST, (o, u) -> {
            o.setMethod(HttpOpener.Method.POST);
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformPostRequestWithBodyParameter() throws IOException {
        shouldPerformRequest(TEST_STRING, HttpOpener.Method.POST, (o, u) -> {
            o.setMethod(HttpOpener.Method.POST);
            o.setUrl(u);
            o.setBody(REQUEST_BODY);
        });
    }

    @Test
    public void shouldPerformPostRequestInsteadOfGetWithBodyParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.POST, (o, u) -> {
            o.setMethod(HttpOpener.Method.GET);
            o.setBody(REQUEST_BODY);
        });
    }

    @Test
    public void shouldPerformPostRequestInsteadOfGetWithInputAsBodyParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.POST, (o, u) -> {
            o.setMethod(HttpOpener.Method.GET);
            o.setUrl(u);
            o.setBody("@-");
        });
    }

    @Test
    public void shouldPerformGetRequestWithoutBodyWithAlreadyUsedInputAsBodyParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {
            o.setBody("@-");
        });
    }

    @Test
    public void shouldPerformPutRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.PUT, (o, u) -> {
            o.setMethod(HttpOpener.Method.PUT);
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformPutRequestWithBodyParameter() throws IOException {
        shouldPerformRequest(TEST_STRING, HttpOpener.Method.PUT, (o, u) -> {
            o.setMethod(HttpOpener.Method.PUT);
            o.setUrl(u);
            o.setBody(REQUEST_BODY);
        });
    }

    @Test
    public void shouldPerformDeleteRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.DELETE, (o, u) -> {
            o.setMethod(HttpOpener.Method.DELETE);
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformHeadRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.HEAD, (o, u) -> {
            o.setMethod(HttpOpener.Method.HEAD);
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformOptionsRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.OPTIONS, (o, u) -> {
            o.setMethod(HttpOpener.Method.OPTIONS);
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformTraceRequestWithUrlParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.TRACE, (o, u) -> {
            o.setMethod(HttpOpener.Method.TRACE);
            o.setUrl(u);
        });
    }

    @Test
    public void shouldPerformGetRequestWithAcceptParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {
            o.setAccept(TEST_STRING);
        }, "Accept");
    }

    @Test
    public void shouldPerformGetRequestWithSingleValuedHeaderParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {
            o.setHeader("x-api-key: " + TEST_STRING);
        }, "x-api-key");
    }

    @Test
    public void shouldPerformGetRequestWithMultiValuedHeaderParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {
            o.setHeader("x-api-key: " + TEST_STRING + "\nx-other-header: " + TEST_STRING);
        }, "x-api-key", "x-other-header");
    }

    @Test
    public void shouldPerformGetRequestWithMultipledHeaderParameters() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {
            o.setHeader("x-api-key: " + TEST_STRING);
            o.setHeader("x-other-header: " + TEST_STRING);
        }, "x-api-key", "x-other-header");
    }

    @Test
    public void shouldPerformPostRequestWithContentTypeParameter() throws IOException {
        shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.POST, (o, u) -> {
            o.setMethod(HttpOpener.Method.POST);
            o.setUrl(u);
            o.setContentType(TEST_STRING);
        }, "Content-Type");
    }

    @Test
    public void shouldPerformPostRequestWithCharsetParameter() throws IOException {
        final String charset = "ISO-8859-1";
        final String header = "Accept-Charset";
        final StringValuePattern value = WireMock.equalTo(charset);

        try {
            shouldPerformRequest(REQUEST_BODY, HttpOpener.Method.POST, (o, u) -> {
                o.setMethod(HttpOpener.Method.POST);
                o.setUrl(u);
                o.setAcceptCharset(charset);
            }, s -> s.withHeader(header, value), q -> q.withHeader(header, value), null);
        }
        catch (final ComparisonFailure e) {
            Assert.assertEquals("expected:<response b[ö]dy> but was:<response b[Ã¶]dy>", e.getMessage());
        }
    }

    @Test
    public void shouldPerformGetRequestWithErrorResponse() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> {},
                null, null, WireMock.badRequest().withBody(RESPONSE_BODY), "ERROR: " + RESPONSE_BODY);
    }

    @Test
    public void shouldPerformGetRequestWithErrorResponseAndErrorPrefixParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> o.setErrorPrefix(TEST_STRING),
                null, null, WireMock.badRequest().withBody(RESPONSE_BODY), TEST_STRING + RESPONSE_BODY);
    }

    @Test
    public void shouldPerformGetRequestWithErrorResponseAndWithoutErrorPrefixParameter() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> o.setErrorPrefix(null),
                null, null, WireMock.badRequest().withBody(RESPONSE_BODY), RESPONSE_BODY);
    }

    @Test
    public void shouldPerformGetRequestWithGzipedContentEncoding() throws IOException {
        shouldPerformRequest(TEST_URL, HttpOpener.Method.GET, (o, u) -> o.setAcceptEncoding("gzip"),
                             null, null,
                             WireMock.ok().withBody(GZIPPED_RESPONSE_BODY).withHeaders(new HttpHeaders(new HttpHeader(HttpOpener.CONTENT_ENCODING_HEADER,"gzip"))),
                             RESPONSE_BODY);
    }

    private void shouldPerformRequest(final String input, final HttpOpener.Method method, final BiConsumer<HttpOpener, String> consumer, final String... headers) throws IOException {
        shouldPerformRequest(input, method, consumer,
                s -> Arrays.stream(headers).forEach(h -> s.withHeader(h, TEST_VALUE)),
                q -> Arrays.stream(headers).forEach(h -> q.withHeader(h, TEST_VALUE)), null);
    }

    private void shouldPerformRequest(final String input, final HttpOpener.Method method, final BiConsumer<HttpOpener, String> consumer, final Consumer<MappingBuilder> stubConsumer, final Consumer<RequestPatternBuilder> requestConsumer, final Consumer<ResponseDefinitionBuilder> responseConsumer) throws IOException {
        final ResponseDefinitionBuilder response = WireMock.ok().withBody(RESPONSE_BODY);
        if (responseConsumer != null) {
            responseConsumer.accept(response);
        }
        shouldPerformRequest(input, method,
                consumer, stubConsumer, requestConsumer,
                response, method.getResponseHasBody() ? RESPONSE_BODY : "");
    }

    private void shouldPerformRequest(final String input, final HttpOpener.Method method, final BiConsumer<HttpOpener, String> consumer, final Consumer<MappingBuilder> stubConsumer, final Consumer<RequestPatternBuilder> requestConsumer, final ResponseDefinitionBuilder response, final String responseBody) throws IOException {
        final String baseUrl = wireMockRule.baseUrl();
        final String url = String.format(TEST_URL, baseUrl);

        final String methodName = method.name();
        final UrlPattern urlPattern = WireMock.urlPathEqualTo(TEST_PATH);

        final HttpOpener opener = new HttpOpener();
        opener.setReceiver(receiver);
        consumer.accept(opener, url);

        final MappingBuilder stub = WireMock.request(methodName, urlPattern).willReturn(response);
        if (stubConsumer != null) {
            stubConsumer.accept(stub);
        }

        final RequestPatternBuilder request = new RequestPatternBuilder(RequestMethod.fromString(methodName), urlPattern)
            .withRequestBody(method.getRequestHasBody() ? WireMock.equalTo(REQUEST_BODY) : WireMock.absent());
        if (requestConsumer != null) {
            requestConsumer.accept(request);
        }

        WireMock.stubFor(stub);

        opener.process(String.format(input, baseUrl));

        // use the opener a second time in a workflow:
        opener.process(String.format(input, baseUrl));

        opener.closeStream();

        WireMock.verify(request);

        Mockito.verify(receiver, times(2)).process(processedObject.capture());
        Assert.assertEquals(responseBody, ResourceUtil.readAll(processedObject.getValue()));
    }

}
