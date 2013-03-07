/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.converter.xml;

import static org.junit.Assert.fail;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.DataFilePath;
import org.culturegraph.mf.stream.converter.CGTextDecoder;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.junit.Test;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 */
public final class GenericXMLHandlerTest {

	@Test
	public void ignoreCharDataNotInRecord() {
		
		final CGTextDecoder decoder = new CGTextDecoder();
		final EventList expectedStream = new EventList();
		
		decoder.setReceiver(expectedStream);
		
		decoder.process("1={ id=1, del=no, name={ value='Record 1' }, description={ lang=de, value='Erster Datensatz' } }");
		decoder.process("2={ id=2, del=yes, name={ value='Record 2' }, description={ lang=de, value='Zweiter Datensatz' } }");
		decoder.closeStream();
		
		final ResourceOpener opener = new ResourceOpener();
		final XmlDecoder saxReader = new XmlDecoder();
		final GenericXmlHandler genericXmlHandler = new GenericXmlHandler("record");
		final StreamValidator validator = new StreamValidator(expectedStream.getEvents());
		validator.setStrictRecordOrder(true);
		validator.setStrictKeyOrder(true);
		validator.setStrictValueOrder(true);
		
		opener.setReceiver(saxReader)
				.setReceiver(genericXmlHandler)
				.setReceiver(validator);
		
		try {
			opener.process(DataFilePath.GENERIC_XML);
			opener.closeStream();
		} catch(FormatException e) {
			fail(e.toString());
		}
	}
}
