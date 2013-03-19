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

import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link JsonEncoder}.
 * 
 * @author Christoph Böhme
 *
 */
public final class JsonEncoderTest {

	private JsonEncoder encoder;
	
	@Mock
	private ObjectReceiver<String> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		encoder = new JsonEncoder();
		encoder.setReceiver(receiver); 
	}
	
	@After
	public void cleanup() {
		encoder.closeStream();
	}
	
	@Test
	public void testShouldEncodeLiteralsAndEntities() {
		encoder.startRecord("");
		encoder.literal("lit1", "val1");
		encoder.startEntity("ent1");
		encoder.literal("lit2", "val2");
		encoder.literal("lit3", "val3");
		encoder.endEntity();
		encoder.endRecord();
		
		verify(receiver).process(fixQuotes("{'lit1':'val1','ent1':{'lit2':'val2','lit3':'val3'}}"));
	}
	
	@Test
	public void testShouldEncodeMarkedEntitiesAsList() {	
		encoder.startRecord("");
		encoder.startEntity("list[]");
		encoder.literal("a", "1");
		encoder.literal("b", "2");
		encoder.literal("c", "3");
		encoder.endEntity();
		encoder.endRecord();
		
		verify(receiver).process(fixQuotes("{'list':['1','2','3']}"));
	}

	/*
	 * Utility method which replaces all single quotes in a string with double quotes.
	 * This allows to specify the JSON output in the test cases without having to wrap
	 * each bit of text in escaped double quotes.
	 */
	private String fixQuotes(final String str) {
		return str.replace('\'', '"');
	}
}
