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

import org.culturegraph.mf.stream.converter.CGTextDecoder;
import org.culturegraph.mf.stream.converter.CGTextEncoder;
import org.culturegraph.mf.stream.pipe.ObjectLogger;
import org.culturegraph.mf.stream.pipe.StreamLogger;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.stream.source.EventListSource;
import org.junit.Test;


/**
 * @author Christoph Böhme
 */
public final class CGTextEncoderTest {

	@Test
	public void test() {
		final EventList expected = new EventList();
		expected.startRecord("1");
			expected.literal("firstName", "Karl Gustav");
			expected.literal("lastName", "Heiligenberg");
			expected.startEntity("placeOfBirth");
				expected.literal("id", "60366");
				expected.startEntity("name");
					expected.literal("descriptor", "Frankfurt");
					expected.literal("qualifier", "Main");
					expected.literal("commonName", "Frankfurt 'am Main'");
				expected.endEntity();
			expected.endEntity();
		expected.endRecord();
		expected.startRecord("2");
			expected.literal("firstname", "Karla");
			expected.literal("lastname", "Gegental");
			expected.literal("full name", "Gegental, Karla");
		expected.endRecord();
		expected.closeStream();
		
		final EventListSource source = new EventListSource(expected.getEvents());
		final CGTextEncoder encoder = new CGTextEncoder();
		final ObjectLogger<String> encoderLog = new ObjectLogger<String>("encoder");
		final CGTextDecoder decoder = new CGTextDecoder();
		final StreamLogger decoderLog = new StreamLogger("decoder");
		final StreamValidator validator = new StreamValidator(expected.getEvents());
		
		source.setReceiver(encoder)
				.setReceiver(encoderLog)
				.setReceiver(decoder)
				.setReceiver(decoderLog)
				.setReceiver(validator);
		
		source.replay();
		source.closeStream();
	}

}
