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

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;


public final class SruOpenerTest {

    private static StringBuilder resultCollector = new StringBuilder();
    private static final String RESPONSE_BODY = "response bödy"; // UTF-8
    private static final String TEST_URL = "/test/path";
    private static SruOpener sruOpener = new SruOpener();


    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().
                                                                             jettyAcceptors(Runtime.getRuntime()
                                                                                                   .availableProcessors())
                                                                             .dynamicPort());

    @Mock
    private ObjectReceiver<Reader> receiver;

    public SruOpenerTest() {
    }

    @Before
    public void setUp() {
        sruOpener = new  SruOpener();
        final char[] buffer = new char[ 1024 * 1024 * 16];
        sruOpener.setReceiver(new DefaultObjectPipe<Reader, ObjectReceiver<String>>() {
            @Override
            public void process(final Reader reader) {
                int size;
                try {
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            resultCollector.append(line+"\n");
                        }
                }
                catch (final IOException e) {
                    throw new MetafactureException(e);
                }
            }
        });
    }


    @Test
    public void test_(){

        // sruOpener.setQuery("dnb.isil%3DDE-Sol1");
        sruOpener.setQuery("WVN%3D24A05");
        sruOpener.setRecordSchema("MARC21plus-xml");
        sruOpener.setVersion("1.1");
        sruOpener.setStartRecord("1890");
        sruOpener.setMaximumRecords("1");
        sruOpener.setTotal("3");
        sruOpener.process("https://services.dnb.de/sru/dnb");
        System.out.println(resultCollector.toString());
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


    //mach lieber wie in metafix/src/test/java/org/metafacture/metafix/MetafixLookupTest.java wiremock
   /* private void shouldPerformRequest(String input, SruOpener sruOpener)  throws IOException { // checkstyle-disable-line ParameterNumber

        final BiConsumer<SruOpener, String> consumer = null;
        final Consumer<MappingBuilder> stubConsumer;
        final Consumer<RequestPatternBuilder> requestConsumer;
        final Consumer<ResponseDefinitionBuilder> responseConsumer = null;
        final String responseBody;
        final ResponseDefinitionBuilder response = WireMock.ok().withBody(RESPONSE_BODY);
        if (responseConsumer != null) {
            responseConsumer.accept(response);
        }

        final String baseUrl = wireMockRule.baseUrl();
        final String url = String.format(TEST_URL, baseUrl);

        final UrlPattern urlPattern = WireMock.urlPathEqualTo(TEST_URL);

        final SruOpener opener = new SruOpener();
        opener.setReceiver(receiver);
        final MappingBuilder stub = WireMock.request("GET", urlPattern).willReturn(response);
        if (stubConsumer != null) {
            stubConsumer.accept(stub);
        }

        final RequestPatternBuilder request = new RequestPatternBuilder(RequestMethod.fromString("GET"), urlPattern)
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
    }
*/

    @Test
    public void test(){
        SruOpener    sruOpener = new SruOpener();
        RecordReader recordReader = new RecordReader();
        recordReader.setReceiver(new ObjectStdoutWriter<String>());
        sruOpener.setReceiver(recordReader);// {


           /* @Override
            public void process(final XmlReceiver obj) {
              BufferedReader in = new BufferedReader(obj);
                String line = null;
                StringBuilder rslt = new StringBuilder();
                while (true) {
                    try {
                        if (!((line = in.readLine()) != null)) break;
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }final InOrder ordered = Mockito.inOrder(receiver);

                    rslt.append(line);
                }*/
      /*          StreamLiteralFormatter streamLiteralFormatter = new StreamLiteralFormatter();
                ObjectStdoutWriter<String> objectStdoutWriter = new ObjectStdoutWriter<String>();
                XmlElementSplitter xmlElementSplitter = new XmlElementSplitter();
                streamLiteralFormatter.setReceiver(objectStdoutWriter);
                xmlElementSplitter.setReceiver(streamLiteralFormatter);
                xmlDecoder.setReceiver(xmlElementSplitter);*/
             //   System.out.println(rslt.toString());
            //    resultCollector.append(obj);
            //}

        sruOpener.setQuery("dnb.isil%3DDE-Sol1");
      //  sruOpener.setQuery("WVN%3D24A05");
        sruOpener.setRecordSchema("MARC21plus-xml");
        sruOpener.setVersion("1.1");
        sruOpener.setStartRecord("3029");
        sruOpener.setMaximumRecords("1");
        sruOpener.setTotal("1");
      //  sruOpener.process("https://services.dnb.de/sru/dnb");
        sruOpener.process("https://services.dnb.de/sru/zdb");
       // sruOpener.process("https://amsquery.stadt-zuerich.ch/sru/");

  //      System.out.println(resultCollector.toString());
    }
}
