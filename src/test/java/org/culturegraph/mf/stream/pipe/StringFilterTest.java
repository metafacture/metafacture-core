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

import org.culturegraph.mf.stream.pipe.ObjectBuffer;
import org.culturegraph.mf.stream.pipe.StringFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Christoph Böhme
 *
 */
public final class StringFilterTest {

	private static final String[] RECORDS = { "Record 1: Data", "Record 42: Data", "Record 3: Data" };
	private static final String PATTERN = "\\d\\d";
	
	private StringFilter filter;
	private ObjectBuffer<String> buffer;

	@Before
	public void configFlow() {
		filter = new StringFilter(PATTERN);
		buffer = new ObjectBuffer<String>();
		filter.setReceiver(buffer);		
	}
	
	@Test
	public void testPassMatches() {
		filter.setPassMatches(true);
		
		processRecords();
		
		Assert.assertEquals(RECORDS[1], buffer.pop());
		Assert.assertNull(buffer.pop());
	}
	
	@Test
	public void testFilterMatches() {
		filter.setPassMatches(false);
		
		processRecords();		

		Assert.assertEquals(RECORDS[0], buffer.pop());
		Assert.assertEquals(RECORDS[2], buffer.pop());
		Assert.assertNull(buffer.pop());
}

	private void processRecords() {
		filter.process(RECORDS[0]);
		filter.process(RECORDS[1]);
		filter.process(RECORDS[2]);
		filter.closeStream();		
	}
}
