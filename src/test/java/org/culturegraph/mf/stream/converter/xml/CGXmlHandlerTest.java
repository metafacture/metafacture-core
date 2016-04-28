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
package org.culturegraph.mf.stream.converter.xml;

import org.culturegraph.mf.stream.DataFilePath;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.junit.Test;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 */
public final class CGXmlHandlerTest {

	private static final String NAME = "Name";
	private static final String ADDRESS = "Address";
	private static final String STREET = "Street";
	private static final String NUMBER = "Number";
	private static final String POSTCODE = "Postcode";
	private static final String CITY = "City";

	@Test
	public void testReadStringStreamReceiver() {
		final ResourceOpener opener = new ResourceOpener();
		final XmlDecoder saxReader = new XmlDecoder();
		final CGXmlHandler cgHandler = new CGXmlHandler();
		final EventList writer = new EventList();


		opener.setReceiver(saxReader)
				.setReceiver(cgHandler)
				.setReceiver(writer);

		opener.process(DataFilePath.CG_XML);
		opener.closeStream();

		final StreamValidator validator = new StreamValidator(writer.getEvents());
		validator.setStrictRecordOrder(true);
		validator.setStrictKeyOrder(true);
		validator.setStrictValueOrder(true);

		validator.startRecord("1");
			validator.literal(NAME, "Thomas Mann");
			validator.startEntity(ADDRESS);
				validator.startEntity(STREET);
					validator.literal(STREET, "Alte Landstrasse");
					validator.literal(NUMBER, "39");
				validator.endEntity();
				validator.literal(CITY, "Kilchberg");
				validator.literal(POSTCODE, null);
			validator.endEntity();
		validator.endRecord();
		validator.startRecord("");
			validator.literal(NAME, "Günter Grass");
			validator.startEntity(ADDRESS);
				validator.startEntity(STREET);
					validator.literal(STREET, "Glockengießerstraße");
					validator.literal(NUMBER, "21");
				validator.endEntity();
				validator.literal(CITY, "Lübeck");
				validator.literal(POSTCODE, "23552");
			validator.endEntity();
		validator.endRecord();
		validator.closeStream();
	}

}
