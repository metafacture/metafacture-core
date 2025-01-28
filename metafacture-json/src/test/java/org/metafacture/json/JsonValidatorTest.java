/*
 * Copyright 2021, 2023 Fabian Steeg, hbz
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

package org.metafacture.json;

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests for {@link JsonValidator}.
 *
 * @author Fabian Steeg
 *
 */
@RunWith(Parameterized.class)
public final class JsonValidatorTest {

    private static final String MAIN_SCHEMA = "/schemas/schema.json";
    private static final String ID_SCHEMA = "/schemas/id.json";
    private static final String JSON_VALID = "{\"id\":\"http://example.org/\"}";
    private static final String JSON_INVALID_MISSING_REQUIRED = "{}";
    private static final String JSON_INVALID_URI_FORMAT = "{\"id\":\"example.org/\"}";
    private static final String JSON_INVALID_DUPLICATE_KEY = "{\"id\":\"val\",\"id\":\"val\"}";
    private static final String JSON_INVALID_SYNTAX_ERROR = "{\"id1\":\"val\",\"id2\":\"val\"";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .jettyAcceptors(Runtime.getRuntime().availableProcessors()).dynamicPort());

    private JsonValidator validator;

    @Mock
    private ObjectReceiver<String> receiver;
    private InOrder inOrder;
    private Function<Object, String> schemaLocationGetter;

    public JsonValidatorTest(final Function<Object, String> schemaLocationGetter) {
        this.schemaLocationGetter = schemaLocationGetter;
    }

    @Parameterized.Parameters(name = "{index}")
    public static Collection<Object[]> siteMaps() {
        return Arrays.asList((Object[][]) (new Function[][] {
            // Pass the schema to each test as path on classpath, file url, and http url:
            {(Object rule) -> MAIN_SCHEMA},
            {(Object rule) -> JsonValidatorTest.class.getResource(MAIN_SCHEMA).toString()},
            {(Object rule) -> ((WireMockRule) rule).baseUrl() + MAIN_SCHEMA}
        }));
    }

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        wireMock(MAIN_SCHEMA, ID_SCHEMA);
        final String schemaLocation = schemaLocationGetter.apply(wireMockRule);
        validator = new JsonValidator(schemaLocation);
        validator.setReceiver(receiver);
        inOrder = Mockito.inOrder(receiver);
    }

    private void wireMock(final String... schemaLocations) throws IOException {
        for (final String schemaLocation : schemaLocations) {
            WireMock.stubFor(WireMock.request("GET", WireMock.urlEqualTo(schemaLocation)).willReturn(
                    WireMock.ok().withBody(readToString(getClass().getResource(schemaLocation)))
                            .withHeader("Content-type", "application/json")));
        }
    }

    private String readToString(final URL url) throws IOException {
        return new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    }

    @Test
    public void callWireMockSchema() throws MalformedURLException, IOException {
        final String schemaContent = readToString(new URL(wireMockRule.baseUrl() + MAIN_SCHEMA));
        Assert.assertThat(schemaContent, CoreMatchers.both(CoreMatchers.containsString("$schema")).and(CoreMatchers.containsString("$ref")));
    }

    @Test
    public void testShouldValidate() {
        validator.process(JSON_VALID);
        inOrder.verify(receiver, Mockito.calls(1)).process(JSON_VALID);
    }

    @Test
    public void testShouldInvalidateMissingRequired() {
        validator.process(JSON_INVALID_MISSING_REQUIRED);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldInvalidateUriFormat() {
        validator.process(JSON_INVALID_URI_FORMAT);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldInvalidateDuplicateKey() {
        validator.process(JSON_INVALID_DUPLICATE_KEY);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldInvalidateSyntaxError() {
        validator.process(JSON_INVALID_SYNTAX_ERROR);
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = MetafactureException.class)
    public void testShouldCatchMissingSchemaFile() {
        new JsonValidator("").process("{}");
    }

    @Test(expected = MetafactureException.class)
    public void testShouldCatchMissingValidOutputFile() {
        validator.setWriteValid("");
        validator.process(JSON_INVALID_MISSING_REQUIRED);
    }

    @Test(expected = MetafactureException.class)
    public void testShouldCatchMissingInvalidOutputFile() {
        validator.setWriteInvalid("");
        validator.process(JSON_INVALID_MISSING_REQUIRED);
    }

    @After
    public void cleanup() {
        validator.closeStream();
    }

}
