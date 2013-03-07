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
package org.culturegraph.mf.stream.pipe;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.converter.CGTextDecoder;
import org.culturegraph.mf.stream.converter.CGTextEncoder;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.util.StreamConstants;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests {@link StreamSerializer}.
 * 
 * @author Christoph Böhme
 */
public final class StreamSerializerTest {

	private static final String RECORD_START = 
			"1={literal1='value1', entity1={literal2=value2}, literal3=value3";
	private static final String RECORD_END = "}";
	private static final String SERIALIZED_LITERAL = ", " + StreamConstants.SERIALIZED + "=";
	private static final String CGE_SERIALIZATION = 
			"'1\u001e-literal1\u001fvalue1\u001e<entity1\u001e-literal2\u001fvalue2\u001e>\u001e-literal3\u001fvalue3\u001e'";
	private static final String CGT_SERIALIZATION = 
			"'1={literal1=value1, entity1={literal2=value2}, literal3=value3}'";
	
	private static final String INPUT = RECORD_START + RECORD_END;
	
	private static final String EXPECTED_CGE = 
			RECORD_START + SERIALIZED_LITERAL + CGE_SERIALIZATION + RECORD_END;
	
	private static final String EXPECTED_CGT = 
			RECORD_START + SERIALIZED_LITERAL + CGT_SERIALIZATION + RECORD_END;

	@Test
	public void testDefaultEncoder() {
		final CGTextDecoder decoder = new CGTextDecoder();
		final EventList expected = new EventList();
		
		decoder.setReceiver(expected);
		
		decoder.process(EXPECTED_CGE);
		decoder.closeStream();
		
		final StreamValidator validator = new StreamValidator(expected.getEvents());
		final StreamSerializer serializer = new StreamSerializer();
				
		decoder.setReceiver(serializer)
				.setReceiver(validator);
		
		decoder.resetStream();
		
		try {
			decoder.process(INPUT);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testCustomEncoder() {
		final CGTextDecoder decoder = new CGTextDecoder();
		final EventList expected = new EventList();
		
		decoder.setReceiver(expected);
		
		decoder.process(EXPECTED_CGT);
		decoder.closeStream();
		
		final StreamValidator validator = new StreamValidator(expected.getEvents());
		final StreamSerializer serializer = new StreamSerializer(new CGTextEncoder());
				
		decoder.setReceiver(serializer)
				.setReceiver(validator);
		
		decoder.resetStream();
		
		try {
			decoder.process(INPUT);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}

}