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

import junit.framework.Assert;

import org.culturegraph.mf.stream.converter.FormetaEncoder;
import org.culturegraph.mf.stream.converter.FormetaEncoder.Style;
import org.culturegraph.mf.stream.pipe.ObjectBuffer;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for {@link FormetaEncoder}.
 * 
 * @author Christoph Böhme
 *
 */
public final class FormetaEncoderTest {

	private static final String CONCISE_RECORD = 
			"1{lit1:value 1,' ent1'{lit2:value \\{x\\},lit\\\\3:'value 2 '}lit4:value \\'3\\'}";
	
	private static final String VERBOSE_RECORD =
			"1{ lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' }";
	
	private static final String MULTILINE_RECORD =
			"'1' {\n" + 
			"\t'lit1': 'value 1',\n" +
			"\t' ent1' {\n" +
			"\t\t'lit2': 'value {x}',\n" +
			"\t\t'lit\\\\3': 'value 2 '\n" +
			"\t},\n" +
			"\t'lit4': 'value \\'3\\''\n" +
			"}";

	private static final String INNER_RECORD = 
			"inner{ lit1: value 1, ent1{ lit2: 'hello worlds\\'s end!' } }";
	
	private static final String OUTER_RECORD = 
			"outer{" +
			"nested:inner\\{ lit1\\: value 1\\, ent1\\{ lit2\\: \\'hello worlds\\\\\\'s end!\\' \\} \\}," +
			"note:nested records" +
			"}";
	
	private static final String BUFFER_NOT_EMPTY_MSG = "The buffer contains more records than expected";
	
	private FormetaEncoder encoder;
	private ObjectBuffer<String> buffer;
	
	@Before
	public void setupFlux() {
		encoder = new FormetaEncoder();
		buffer = new ObjectBuffer<String>();
		
		encoder.setReceiver(buffer);
	}
	
	@Test
	public void testConcise() {	
		encoder.setStyle(Style.CONCISE);
		
		runEventSequence();
		
		Assert.assertEquals(CONCISE_RECORD, buffer.pop());
		Assert.assertNull(BUFFER_NOT_EMPTY_MSG, buffer.pop());
	}

	@Test
	public void testVerbose() {
		encoder.setStyle(Style.VERBOSE);
		
		runEventSequence();
		
		Assert.assertEquals(VERBOSE_RECORD, buffer.pop());
		Assert.assertNull(BUFFER_NOT_EMPTY_MSG, buffer.pop());
	}

	@Test
	public void testMultiline() {
		encoder.setStyle(Style.MULTILINE);
		
		runEventSequence();
		
		Assert.assertEquals(MULTILINE_RECORD, buffer.pop());
		Assert.assertNull(BUFFER_NOT_EMPTY_MSG, buffer.pop());
	}
	
	@Test
	public void testIncompleteRecord() {
		encoder.setStyle(Style.CONCISE);
		
		encoder.startRecord("incomplete");
		encoder.literal("lit", "value");
		encoder.startEntity("entity");
		runEventSequence();
		
		Assert.assertEquals(CONCISE_RECORD, buffer.pop());
		Assert.assertNull(BUFFER_NOT_EMPTY_MSG, buffer.pop());
	}
	
	@Test
	public void testNestedRecords() {
		encoder.setStyle(Style.CONCISE);
		
		encoder.startRecord("outer");
		encoder.literal("nested", INNER_RECORD);
		encoder.literal("note", "nested records");
		encoder.endRecord();
		encoder.closeStream();
		
		Assert.assertEquals(OUTER_RECORD, buffer.pop());
		Assert.assertNull(BUFFER_NOT_EMPTY_MSG, buffer.pop());
	}

	private void runEventSequence() {
		encoder.startRecord("1");
		encoder.literal("lit1", "value 1");
		encoder.startEntity(" ent1");
		encoder.literal("lit2", "value {x}");
		encoder.literal("lit\\3", "value 2 ");
		encoder.endEntity();
		encoder.literal("lit4", "value '3'");
		encoder.endRecord();
		encoder.closeStream();
	}
}
