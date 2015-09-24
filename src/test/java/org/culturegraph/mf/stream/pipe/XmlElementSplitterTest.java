/* Copyright 2013  Pascal Christoph, hbz
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

package org.culturegraph.mf.stream.pipe;

import java.io.File;
import java.net.URISyntaxException;

import org.culturegraph.mf.stream.converter.xml.XmlDecoder;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.stream.source.FileOpener;
import org.junit.Test;

/**
 * @author Pascal Christoph (dr0i)
 *
 */
@SuppressWarnings("javadoc")
public class XmlElementSplitterTest {

	@Test
	public void testFlow() throws URISyntaxException {
		final FileOpener opener = new FileOpener();
		final XmlDecoder xmldecoder = new XmlDecoder();
		final XmlElementSplitter xmlsplitter = new XmlElementSplitter();
		xmlsplitter.setElementName("Description");
		xmlsplitter.setTopLevelElement("rdf:RDF");
		final EventList expected = new EventList();
		expected.startRecord("0");
		expected.literal("Element", xmlsplitter.getXmlDeclaration()
				+ "<rdf:RDF xmlns:rdf=\"ns#\"><rdf:Description rdf:about=\"1\"> <a rdf:resource=\"r1\">1</a></rdf:Description></rdf:RDF>");
		expected.endRecord();
		expected.startRecord("1");
		expected.literal("Element", xmlsplitter.getXmlDeclaration()
				+ "<rdf:RDF xmlns:rdf=\"ns#\"><rdf:Description rdf:about=\"2\"> <a rdf:resource=\"r2\">2</a></rdf:Description></rdf:RDF>");
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(expected.getEvents());
		opener.setReceiver(xmldecoder).setReceiver(xmlsplitter).setReceiver(validator);
		File infile = new File(
				Thread.currentThread().getContextClassLoader().getResource("data/xmlToBeSplitted.xml").toURI());
		opener.process(infile.getAbsolutePath());
		opener.closeStream();
	}
}
