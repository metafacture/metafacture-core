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
package org.culturegraph.mf.stream.pipe;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.converter.CGTextDecoder;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test {@link StreamBatchMerger}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamBatchMergerTest {

	private static final String INPUT1 = "0={lit0=A}";
	private static final String INPUT2 = "1={lit1=B}";
	private static final String INPUT3 = "2={lit2=C, ent2={ent2lit1=X}}";
	private static final String INPUT4 = "3={lit3=D}";
	private static final String INPUT5 = "4={lit4=E}";

	private static final String EXPECTED_RESULT1 = "0={lit0=A, lit1=B}";
	private static final String EXPECTED_RESULT2 = "2={lit2=C, ent2={ent2lit1=X}, lit3=D}";
	private static final String EXPECTED_RESULT3 = INPUT5;

	@Test
	public void testShouldMergeNConsecutiveRecords() {
		final CGTextDecoder decoder = new CGTextDecoder();
		final EventList buffer = new EventList();

		decoder.setReceiver(buffer);

		decoder.process(EXPECTED_RESULT1);
		decoder.process(EXPECTED_RESULT2);
		decoder.process(EXPECTED_RESULT3);
		//decoder.closeStream();

		final StreamBatchMerger merger = new StreamBatchMerger();
		merger.setBatchSize(2);
		final StreamValidator validator = new StreamValidator(buffer.getEvents());

		decoder.setReceiver(merger).setReceiver(validator);

		try {
			decoder.process(INPUT1);
			decoder.process(INPUT2);
			decoder.process(INPUT3);
			decoder.process(INPUT4);
			decoder.process(INPUT5);
			decoder.closeStream();
		} catch(FormatException e) {
			Assert.fail(e.toString());
		}
	}

}
