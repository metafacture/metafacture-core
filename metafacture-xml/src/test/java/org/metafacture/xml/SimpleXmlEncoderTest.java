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

package org.metafacture.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.helpers.DefaultObjectReceiver;

/**
 * Tests for class {@link SimpleXmlEncoder}.
 *
 * @author Markus Geipel
 * @author Christoph Böhme
 *
 */
public final class SimpleXmlEncoderTest {

    private static final String TAG = "tag";
    private static final String VALUE = "value";

    private SimpleXmlEncoder simpleXmlEncoder;

    private StringBuilder resultCollector;

    @Before
    public void initSystemUnderTest() {
        simpleXmlEncoder = new SimpleXmlEncoder();
        simpleXmlEncoder.setReceiver(
                new DefaultObjectReceiver<String>() {
                    @Override
                    public void process(final String obj) {
                        resultCollector.append(obj);
                    }
                });
        resultCollector = new StringBuilder();
    }

    @Test
    public void issue249_shouldNotEmitClosingRootTagOnCloseStreamIfNoOutputWasGenerated() {
        simpleXmlEncoder.closeStream();

        assertTrue(getResultXml().isEmpty());
    }

    @Test
    public void shouldNotEmitClosingRootTagOnResetStreamIfNoOutputWasGenerated() {
        simpleXmlEncoder.resetStream();

        assertTrue(getResultXml().isEmpty());
    }

    @Test
    public void shouldOnlyEscapeXmlReservedCharacters() {
        final StringBuilder builder = new StringBuilder();

        SimpleXmlEncoder.writeEscaped(builder , "&<>'\" üäö");

        assertEquals("&amp;&lt;&gt;&apos;&quot; üäö", builder.toString());
    }

    @Test
    public void shouldWrapEachRecordInRootTagIfSeparateRootsIsTrue() {
        simpleXmlEncoder.setSeparateRoots(true);

        emitTwoRecords();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><records><record><tag>value</tag></record></records><?xml version=\"1.0\" encoding=\"UTF-8\"?><records><record><tag>value</tag></record></records>",
                getResultXml());
    }

    @Test
    public void shouldWrapAllRecordsInOneRootTagtIfSeparateRootsIsFalse() {
        simpleXmlEncoder.setSeparateRoots(false);

        emitTwoRecords();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><records><record><tag>value</tag></record><record><tag>value</tag></record></records>",
                getResultXml());
    }

    @Test
    public void shouldAddNamespaceToRootElement() {
        final Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns", "http://example.org/ns");
        simpleXmlEncoder.setNamespaces(namespaces);

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><records xmlns:ns=\"http://example.org/ns\"><record /></records>",
                getResultXml());
    }

    @Test
    public void shouldAddNamespaceWithEmptyKeyAsDefaultNamespaceToRootTag() {
        final Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("", "http://example.org/ns");
        simpleXmlEncoder.setNamespaces(namespaces);

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><records xmlns=\"http://example.org/ns\"><record /></records>",
                getResultXml());
    }

    @Test
    public void shouldAddNamespaceWithEmptyKeyFromPropertiesFileAsDefaultNamespaceToRootTag() {
        simpleXmlEncoder.setNamespaceFile("org/metafacture/xml/SimpleXmlEncoderTest_namespaces.properties");

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><records xmlns=\"http://example.org/ns\"><record /></records>",
                getResultXml());
    }

    @Test
    public void shouldNotEmitRootTagIfWriteRootTagIsFalse() {
        simpleXmlEncoder.setWriteRootTag(false);

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><record />",
                getResultXml());
    }

    @Test
    public void shouldAddNamespacesToRecordTagIfWriteRootTagIsFalse() {
        final Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns", "http://example.org/ns");
        simpleXmlEncoder.setNamespaces(namespaces);
        simpleXmlEncoder.setWriteRootTag(false);

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><record  xmlns:ns=\"http://example.org/ns\" />",
                getResultXml());
    }

    @Test
    public void shouldAddNamespaceWithEmptyKeyAsDefaultNamespaceToRecordTag() {
        final Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("", "http://example.org/ns");
        simpleXmlEncoder.setNamespaces(namespaces);
        simpleXmlEncoder.setWriteRootTag(false);

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><record  xmlns=\"http://example.org/ns\" />",
                getResultXml());
    }

    @Test
    public void shouldAddNamespaceWithEmptyKeyFromPropertiesFileAsDefaultNamespaceToRecordTag() {
        simpleXmlEncoder.setNamespaceFile("org/metafacture/xml/SimpleXmlEncoderTest_namespaces.properties");
        simpleXmlEncoder.setWriteRootTag(false);

        emitEmptyRecord();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><record  xmlns=\"http://example.org/ns\" />",
                getResultXml());
    }

    @Test
    public void testShouldEncodeUnnamedLiteralsAsText() {
        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal("", VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record>" +
                "value" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void testShouldStillEncodeUnnamedLiteralsAsTextWithConfiguredValueTagName() {
        simpleXmlEncoder.setValueTag("data");

        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal("", VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        // SimpleXmlEncoder.Element.writeElement() does not write child elements with empty name
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record>" +
                "value" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void testShouldNotEncodeLiteralsWithDifferentValueTagNameAsText() {
        simpleXmlEncoder.setValueTag("data");

        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record>" +
                "<tag>value</tag>" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void issue379_testShouldEncodeConfiguredValueLiteralsAsText() {
        final String name = "data";
        simpleXmlEncoder.setValueTag(name);

        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal(name, VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record>" +
                "value" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void testShouldEncodeMarkedLiteralsAsAttributes() {
        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.literal("~attr", VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record attr=\"value\">" +
                "<tag>value</tag>" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void testShouldNotEncodeMarkedEntitiesAsAttributes() {
        simpleXmlEncoder.setAttributeMarker("*");

        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.startEntity("~entity");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.endEntity();
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record>" +
                "<~entity>" +
                "<tag>value</tag>" +
                "</~entity>" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void testShouldNotEncodeLiteralsWithDifferentMarkerAsAttributes() {
        simpleXmlEncoder.setAttributeMarker("*");

        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.literal("~attr", VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record>" +
                "<tag>value</tag>" +
                "<~attr>value</~attr>" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    @Test
    public void testShouldEncodeMarkedLiteralsWithConfiguredMarkerAsAttributes() {
        final String marker = "**";
        simpleXmlEncoder.setAttributeMarker(marker);

        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.literal(marker + "attr", VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<records>" +
                "<record attr=\"value\">" +
                "<tag>value</tag>" +
                "</record>" +
                "</records>",
                getResultXml());
    }

    private void emitTwoRecords() {
        simpleXmlEncoder.startRecord("X");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.startRecord("Y");
        simpleXmlEncoder.literal(TAG, VALUE);
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();
    }

    private void emitEmptyRecord() {
        simpleXmlEncoder.startRecord("");
        simpleXmlEncoder.endRecord();
        simpleXmlEncoder.closeStream();
    }

    private String getResultXml() {
        return resultCollector.toString().replaceAll("[\\n\\t]", "");
    }

}
