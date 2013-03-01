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
import org.culturegraph.mf.stream.converter.FormetaDecoder;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link FormetaDecoder}.
 * 
 * @author Christoph Böhme
 * 
 */
public final class FormetaDecoderTest {

	private static final String CONCISE_RECORD = 
			"1{lit1:value 1,' ent1'{lit2:value \\{x\\},lit\\\\3:'value 2 '}lit4:value \\'3\\'}";
	
	private static final String VERBOSE_RECORD =
			"1{ lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' }";
	
	private static final String MULTILINE_RECORD =
			"1{\n" + 
			"  lit1: 'value 1',\n" +
			"  ' ent1'{\n" +
			"    lit2: 'value {x}',\n" +
			"    'lit\\\\3': 'value 2 '\n" +
			"  },\n" +
			"  lit4: 'value \\'3\\''\n" +
			"}";
	
	private static final String BROKEN_RECORD = 
			"1 { lit1: 'value 1',";

	private static final String INNER_RECORD = 
			"inner{ lit1: value 1, ent1{ lit2: 'hello worlds\\'s end!' } }";
	
	private static final String OUTER_RECORD = 
			"outer{" +
			"nested:inner\\{ lit1\\: value 1\\, ent1\\{ lit2\\: \\'hello worlds\\\\\\'s end!\\' \\} \\}," +
			"note:I can has nezted records" +
			"}";
	
	private static final String PARTIAL_RECORD = 
			 "lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' ";
	
	private static final String BROKEN_PARTIAL_RECORD =
			 "lit1: 'value 1', ' ent1'{ lit2: 'value {x}'";

	private FormetaDecoder decoder;
	private StreamValidator validator;

	@Test
	public void testConcise() {
		setupFlux(createEventList());
		try {
			decoder.process(CONCISE_RECORD);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testVerbose() {
		setupFlux(createEventList());
		try {
			decoder.process(VERBOSE_RECORD);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testMultiline() {
		setupFlux(createEventList());
		try {
			decoder.process(MULTILINE_RECORD);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}
	
	@Test
	public void testItemSeparatorAfterRecord() {
		setupFlux(createEventList());
		try {
			decoder.process(CONCISE_RECORD + ", ");
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}
	
	@Test(expected=FormatException.class)
	public void testDoubleCloseRecord() {
		final EventList eventList = new EventList();
		eventList.startRecord("1");
		eventList.literal("lit", "val");
		eventList.endRecord();
		setupFlux(eventList);
		decoder.process("1 { lit: val }}");
		decoder.closeStream();
		
	}
	
	@Test(expected=FormatException.class)
	public void testGarbageAfterRecord() {
		setupFlux(createEventList());
		decoder.process(CONCISE_RECORD + "Garbage");
		decoder.closeStream();
	}
	
	@Test
	public void testTwoRecords() {
		final EventList eventList = new EventList();
		execTestRecordEvents(eventList);
		execTestRecordEvents(eventList);
		setupFlux(eventList);
		try {
			decoder.process(CONCISE_RECORD + CONCISE_RECORD);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}		
	}

	@Test(expected=FormatException.class)
	public void testIncompleteRecord() {
		setupFlux(createEventList());
		decoder.process(BROKEN_RECORD);
		decoder.closeStream();
	}
	
	@Test
	public void testRecoverAfterIncompleteRecord() {
		// Try processing an incomplete record:
		setupFlux(createEventList());
		try {
			decoder.process(BROKEN_RECORD);
		} catch (FormatException e) {
			// The decoder should recover automatically
		}
		
		// Test whether another record can be processed
		// afterwards:
		
		decoder.setReceiver(new StreamValidator(createEventList().getEvents()));
		
		decoder.process(CONCISE_RECORD);
		decoder.closeStream();
	}
	
	@Test
	public void testNestedRecords() {
		final EventList eventList = new EventList();
		eventList.startRecord("outer");
		eventList.literal("nested", INNER_RECORD);
		eventList.literal("note", "I can has nezted records");
		eventList.endRecord();
		eventList.closeStream();
		setupFlux(eventList);
		try {
			decoder.process(OUTER_RECORD);
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());			
		}
	}
	
	@Test
	public void testPartialRecord() {
		decoder = new FormetaDecoder(FormetaDecoder.Mode.PARTIAL_RECORDS);
		validator = new StreamValidator(createEventList().getEvents());
		decoder.setReceiver(validator);
		try {
			validator.startRecord("1");
			decoder.process(PARTIAL_RECORD);
			validator.endRecord();
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());			
		}		
	}
	
	@Test(expected=FormatException.class)
	public void testIncompletePartialRecord() {
		decoder = new FormetaDecoder(FormetaDecoder.Mode.PARTIAL_RECORDS);
		validator = new StreamValidator(createEventList().getEvents());
		decoder.setReceiver(validator);
		
		validator.startRecord("1");
		decoder.process(BROKEN_PARTIAL_RECORD);
		validator.endRecord();
		decoder.closeStream();
	}

	private void setupFlux(final EventList expected) {
		decoder = new FormetaDecoder();
		validator = new StreamValidator(expected.getEvents());
		decoder.setReceiver(validator);
	}

	private EventList createEventList() {
		final EventList list = new EventList();
		execTestRecordEvents(list);
		list.closeStream();
		return list;
	}
	
	private void execTestRecordEvents(final EventList list) {		
		list.startRecord("1");
		list.literal("lit1", "value 1");
		list.startEntity(" ent1");
		list.literal("lit2", "value {x}");
		list.literal("lit\\3", "value 2 ");
		list.endEntity();
		list.literal("lit4", "value '3'");
		list.endRecord();
	}
	
}
