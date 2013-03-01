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
package org.culturegraph.mf.stream.converter;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.converter.ObjectToLiteral;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Christoph Böhme
 *
 */
public final class ObjectToLiteralTest {
	
	private static final String LITERAL_NAME = "myObject";
	private static final String OBJ_DATA = "This is a data object";
	
	@Test
	public void test() {
		final EventList buffer = new EventList();
		buffer.startRecord(null);
		buffer.literal(LITERAL_NAME, OBJ_DATA);
		buffer.endRecord();
		buffer.closeStream();
		
		final ObjectToLiteral<String> objectToLiteral = new ObjectToLiteral<String>();
		objectToLiteral.setLiteralName(LITERAL_NAME);
		final StreamValidator validator = new StreamValidator(buffer.getEvents());
		
		objectToLiteral.setReceiver(validator);
		
		try {
			objectToLiteral.process(OBJ_DATA);
			objectToLiteral.closeStream();
		} catch(FormatException e) {
			Assert.fail(e.toString());
		}
	}

}
