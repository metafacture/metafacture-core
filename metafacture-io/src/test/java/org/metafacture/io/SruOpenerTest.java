/*
 * Copyright 2025 hbz
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

import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.ObjectReceiver;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class SruOpenerTest {

    private static final String FILE_PATH = String.format("/%s/%%s", WireMockApp.FILES_ROOT);

    private static final String DNB_URL = "https://services.dnb.de";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .jettyAcceptors(Runtime.getRuntime().availableProcessors()).dynamicPort());

    @Mock
    private ObjectReceiver<Reader> receiver;

    public SruOpenerTest() {
    }

    @Test
    public void testDnb() {
        assertSru(DNB_URL, "/sru/dnb", o -> {
                o.setQuery("WVN=24A05");
                o.setRecordSchema("MARC21plus-xml");
                o.setVersion("1.1");
                o.setStartRecord(1890);
                o.setMaximumRecords(1);
                o.setTotalRecords(3);
            },
            "7ba7099f-ecd2-43f2-ada5-c053f2532da3",
            "e9396103-55d6-41c6-984a-bb656304764a",
            "8ce72c3c-e18f-488a-855d-6dcbf320cb4f"
        );
    }

    @Test
    public void testDnbMaximumRecords2() {
        assertSru(DNB_URL, "/sru/dnb", o -> {
                o.setQuery("WVN=24A05");
                o.setRecordSchema("MARC21plus-xml");
                o.setVersion("1.1");
                o.setStartRecord(1890);
                o.setMaximumRecords(2);
                o.setTotalRecords(3);
            },
            "b0478aa7-2e7c-4301-a463-c189da335428",
            "241be368-d2a1-427a-a554-667b67f69ef8"
        );
    }

    @Test
    public void testZdb() {
        assertSru(DNB_URL, "/sru/zdb", o -> {
                o.setQuery("dnb.isil=DE-Sol1");
                o.setRecordSchema("MARC21plus-xml");
                o.setVersion("1.1");
                o.setStartRecord(3029);
                o.setMaximumRecords(1);
                o.setTotalRecords(1);
            },
            "23b62fc7-de7f-4603-949d-a5e12027a25d"
        );
    }

    private void assertSru(final String url, final String path, final Consumer<SruOpener> consumer, final String... ids) {
        final boolean recording = Boolean.getBoolean("org.metafacture.wiremock.record");
        if (recording) {
            System.out.println("Recording live requests to proxy: " + url);
            WireMock.startRecording(WireMock.recordSpec().forTarget(url).extractTextBodiesOver(0));
        }

        final SruOpener opener = new SruOpener();
        opener.setReceiver(receiver);
        consumer.accept(opener);

        final List<String> responseBodies = new ArrayList<>();
        Arrays.stream(ids).forEach(i -> {
            final StubMapping mapping = wireMockRule.getStubMapping(UUID.fromString(i)).getItem();

            if (mapping == null) {
                System.err.println("WireMock mapping not found: " + i);
            }
            else {
                final String fileName = String.format(FILE_PATH, mapping.getResponse().getBodyFileName());

                try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
                    if (inputStream == null) {
                        System.err.println("Response body not found for " + i + ": " + fileName);
                    }
                    else {
                        responseBodies.add(ResourceUtil.readAll(inputStream, Charset.defaultCharset()));
                    }
                }
                catch (final IOException e) {
                    System.err.println("Failed to load response body for " + i + ": " + e);
                }
            }
        });

        TestHelpers.assertReader(receiver, () -> {
            opener.process(wireMockRule.url(path));
            opener.closeStream();

            if (recording) {
                WireMock.stopRecording();
            }
        }, responseBodies.toArray(new String[responseBodies.size()]));
    }

}
