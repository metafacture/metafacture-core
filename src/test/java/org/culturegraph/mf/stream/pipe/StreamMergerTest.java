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
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for {@link StreamMerger}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamMergerTest {

	private static final String ID1 = "1";
	private static final String ID2 = "2";
	private static final String ENTITY1 = "E1";
	private static final String ENTITY2 = "E2";
	private static final String LITERAL1 = "L1";
	private static final String LITERAL2 = "L2";
	private static final String LITERAL3 = "L3";
	private static final String VALUE1 = "v1";
	private static final String VALUE2 = "v2";
	private static final String VALUE3 = "v3";
	private static final String VALUE4 = "v4";

	private StreamMerger streamMerger;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		streamMerger = new StreamMerger();
		streamMerger.setReceiver(receiver);
	}

	@Test
	public  void testShouldMergeSequencesOfRecordsWithTheSameId() {
		final EventList buffer = new EventList();

		buffer.startRecord(ID1);
			buffer.startEntity(ENTITY1);
				buffer.literal(LITERAL1, VALUE1);
			buffer.endEntity();
			buffer.literal(LITERAL2, VALUE2);
			buffer.literal(LITERAL2, VALUE3);
			buffer.startEntity(ENTITY2);
				buffer.literal(LITERAL3, VALUE4);
			buffer.endEntity();
		buffer.endRecord();
		buffer.closeStream();

		final StreamMerger merger = new StreamMerger();
		final StreamValidator validator = new StreamValidator(buffer.getEvents());

		merger.setReceiver(validator);

		try {
			merger.startRecord(ID1);
				merger.startEntity(ENTITY1);
					merger.literal(LITERAL1, VALUE1);
				merger.endEntity();
				merger.literal(LITERAL2, VALUE2);
			merger.endRecord();
			merger.startRecord(ID1);
				merger.literal(LITERAL2, VALUE3);
				merger.startEntity(ENTITY2);
					merger.literal(LITERAL3, VALUE4);
				merger.endEntity();
			merger.endRecord();
			merger.closeStream();
		} catch(FormatException e) {
			Assert.fail(e.toString());
		}
	}

	@Test
	public  void testNoMerge() {
		final EventList buffer = new EventList();


		buffer.startRecord(ID1);
			buffer.startEntity(ENTITY1);
				buffer.literal(LITERAL1, VALUE1);
			buffer.endEntity();
			buffer.literal(LITERAL2, VALUE2);
		buffer.endRecord();
		buffer.startRecord(ID2);
			buffer.literal(LITERAL2, VALUE3);
			buffer.startEntity(ENTITY2);
				buffer.literal(LITERAL3, VALUE4);
			buffer.endEntity();
		buffer.endRecord();
		buffer.closeStream();

		final StreamMerger merger = new StreamMerger();
		final StreamValidator validator = new StreamValidator(buffer.getEvents());

		merger.setReceiver(validator);

		try {
			merger.startRecord(ID1);
				merger.startEntity(ENTITY1);
					merger.literal(LITERAL1, VALUE1);
				merger.endEntity();
				merger.literal(LITERAL2, VALUE2);
			merger.endRecord();
			merger.startRecord(ID2);
				merger.literal(LITERAL2, VALUE3);
				merger.startEntity(ENTITY2);
					merger.literal(LITERAL3, VALUE4);
				merger.endEntity();
			merger.endRecord();
			merger.closeStream();
		} catch(FormatException e) {
			Assert.fail(e.toString());
		}
	}

}
