/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.test;

import java.io.FileNotFoundException;
import java.io.StringReader;

import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.reader.MultiFormatReader;
import org.culturegraph.mf.stream.reader.Reader;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.reflection.ObjectFactory;
import org.culturegraph.mf.util.xml.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 *
 */
public final class TestCase {

	private static final String NO_DATA_FOUND = "Please specify either element content or a src attribute";

	private static final String NAME_ATTR = "name";
	private static final String IGNORE_ATTR = "ignore";
	private static final String RESULT_TAG = "result";
	private static final String TRANSFORMATION_TAG = "transformation";
	private static final String INPUT_TAG = "input";
	private static final String SRC_ATTR = "src";
	private static final String TYPE_ATTR = "type";
	private static final String STRICT_RECORD_ORDER_ATTR = "strict-record-order";
	private static final String STRICT_KEY_ORDER_ATTR = "strict-key-order";
	private static final String STRICT_VALUE_ORDER_ATTR = "strict-value-order";

	private static final String MIME_METAMORPH = "text/x-metamorph+xml";
	private static final String MIME_JAVACLASS = "application/java";

	private final Element config;

	public TestCase(final Element config) {
		this.config = config;
	}

	public String getName() {
		return config.getAttribute(NAME_ATTR);
	}

	public boolean isIgnore() {
		return Boolean.parseBoolean(config.getAttribute(IGNORE_ATTR));
	}

	public void run() {
		final Reader inputReader = getReader(INPUT_TAG);
		@SuppressWarnings("unchecked")
		final StreamPipe<StreamReceiver>transformation = getTransformation();
		final EventList resultStream = new EventList();

		if (transformation == null) {
			inputReader.setReceiver(resultStream);
		} else {
			inputReader.setReceiver(transformation).setReceiver(resultStream);
		}

		inputReader.process(getInputData());
		inputReader.closeStream();

		final StreamValidator validator = new StreamValidator(resultStream.getEvents());

		final Element result = (Element) config.getElementsByTagName(RESULT_TAG).item(0);
		validator.setStrictRecordOrder(Boolean.parseBoolean(result.getAttribute(STRICT_RECORD_ORDER_ATTR)));
		validator.setStrictKeyOrder(Boolean.parseBoolean(result.getAttribute(STRICT_KEY_ORDER_ATTR)));
		validator.setStrictValueOrder(Boolean.parseBoolean(result.getAttribute(STRICT_VALUE_ORDER_ATTR)));

		final Reader resultReader = getReader(RESULT_TAG);
		resultReader.setReceiver(validator);

		resultReader.process(getExpectedResult());
		validator.closeStream();
	}

	private Reader getReader(final String tag) {
		final Element element = (Element) config.getElementsByTagName(tag).item(0);
		final String mimeType = element.getAttribute(TYPE_ATTR);
		return new MultiFormatReader(mimeType);
	}

	@SuppressWarnings("rawtypes")
	private StreamPipe getTransformation() {
		final NodeList nodes = config.getElementsByTagName(TRANSFORMATION_TAG);
		if (nodes.getLength() == 0) {
			return null;
		}
		final Element transformationElement = (Element) nodes.item(0);
		final java.io.Reader ioReader;

		final String type = transformationElement.getAttribute(TYPE_ATTR);
		final String src = transformationElement.getAttribute(SRC_ATTR);

		if (MIME_METAMORPH.equals(type)) {
			if (src.isEmpty()) {
				ioReader = getDataEmbedded(transformationElement);
			} else {
				ioReader = getDataFromSource(src);
			}
			return new Metamorph(ioReader);

		} else if (MIME_JAVACLASS.equals(type)) {
			if (src.isEmpty()) {
				throw new TestConfigurationException("class defining transformation not specified");
			}
			final Class<? extends StreamPipe> clazz = ObjectFactory.loadClass(src, StreamPipe.class);
			return ObjectFactory.newInstance(clazz);
		}
		throw new TestConfigurationException("transformation of type " + type + " is not supperted");

	}

	private java.io.Reader getInputData() {
		final Element input = (Element) config.getElementsByTagName(INPUT_TAG).item(0);

		if (input.hasAttribute(SRC_ATTR)) {
			return getDataFromSource(input.getAttribute(SRC_ATTR));
		}
		return getDataEmbedded(input);
	}

	private java.io.Reader getExpectedResult() {
		final Element result = (Element) config.getElementsByTagName(RESULT_TAG).item(0);
		if (result.hasAttribute(SRC_ATTR)) {
			return getDataFromSource(result.getAttribute(SRC_ATTR));
		}
		return getDataEmbedded(result);
	}

	private java.io.Reader getDataFromSource(final String src) {
		try {
			return ResourceUtil.getReader(src);
		} catch (final FileNotFoundException e) {
			throw new TestConfigurationException("Could not find input file: " + src, e);
		}
	}

	private java.io.Reader getDataEmbedded(final Element input) {
		final String inputType = input.getAttribute(TYPE_ATTR);
		if (input.hasChildNodes()) {
			if (XmlUtil.isXmlMimeType(inputType)) {
				return new StringReader(XmlUtil.nodeListToString(input.getChildNodes()));
			}
			return new StringReader(input.getTextContent());
		}

		throw new TestConfigurationException(NO_DATA_FOUND);
	}

}
