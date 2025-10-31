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

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.io.Reader;

public final class SruOpenerTest {

    private static final StringBuilder RESULT_COLLECTOR = new StringBuilder();
    private static final String RESPONSE_BODY = "response bödy"; // UTF-8
    private static final String TEST_URL = "/test/path";
    private static final SruOpener SRU_OPENER = new SruOpener();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .jettyAcceptors(Runtime.getRuntime().availableProcessors()).dynamicPort());

    @Mock
    private ObjectReceiver<Reader> receiver;

    public SruOpenerTest() {
    }

    @Before
    public void setUp() {
        final char[] buffer = new char[1024 * 1024 * 16];
        SRU_OPENER.setReceiver(new DefaultObjectPipe<Reader, ObjectReceiver<String>>() {
            @Override
            public void process(final Reader reader) {
                int size;
                try {
                    while ((size = reader.read(buffer)) != -1) {
                        RESULT_COLLECTOR.append(buffer, 0, size);
                    }
                }
                catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test
    public void test1() {
        // sruOpener.setQuery("dnb.isil%3DDE-Sol1");
        SRU_OPENER.setQuery("WVN=24A05");
        SRU_OPENER.setRecordSchema("MARC21plus-xml");
        SRU_OPENER.setVersion("1.1");
        SRU_OPENER.setStartRecord(1890);
        SRU_OPENER.setMaximumRecords(1);
        SRU_OPENER.setTotalRecords(3);
        SRU_OPENER.process("https://services.dnb.de/sru/dnb");
        System.out.println(RESULT_COLLECTOR.toString());
    }

/*    @Test
    public void shouldPerformGetRequestWithInputAsUrlByDefault() throws IOException {
        SruOpener sruOpener = new SruOpener();
        sruOpener.setQuery("WVN%3D24A05");
        sruOpener.setRecordSchema("MARC21plus-xml");
        sruOpener.setVersion("1.1");
        sruOpener.setStartRecord("1890");
        sruOpener.setTotal("32");
        shouldPerformRequest(TEST_URL,sruOpener);
    }*/

    @Test
    public void test() {
        final RecordReader recordReader = new RecordReader();
        recordReader.setReceiver(new ObjectStdoutWriter<String>());
        SRU_OPENER.setReceiver(recordReader);
        SRU_OPENER.setQuery("dnb.isil=DE-Sol1");
        //  sruOpener.setQuery("WVN%3D24A05");
        SRU_OPENER.setRecordSchema("MARC21plus-xml");
        SRU_OPENER.setVersion("1.1");
        SRU_OPENER.setStartRecord(3029);
        SRU_OPENER.setMaximumRecords(1);
        SRU_OPENER.setTotalRecords(1);
        //  sruOpener.process("https://services.dnb.de/sru/dnb");
        SRU_OPENER.process("https://services.dnb.de/sru/zdb");
        // sruOpener.process("https://amsquery.stadt-zuerich.ch/sru/");
        // System.out.println(resultCollector.toString());
    }
}
