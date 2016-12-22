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
package org.culturegraph.mf.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.helpers.DefaultObjectReceiver;
import org.junit.Before;
import org.junit.Test;

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
	public void shouldAddNamespaceWithEmptyKeyAsDefaultNamespace() {
		final Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://example.org/ns");
		simpleXmlEncoder.setNamespaces(namespaces);

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
