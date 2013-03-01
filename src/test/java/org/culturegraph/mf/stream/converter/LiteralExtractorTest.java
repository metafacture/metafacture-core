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

import org.culturegraph.mf.stream.converter.CGTextDecoder;
import org.culturegraph.mf.stream.converter.LiteralExtractor;
import org.culturegraph.mf.stream.pipe.ObjectBuffer;
import org.junit.Test;


/**
 * @author Christoph Böhme
 *
 */
public final class LiteralExtractorTest {

	private static final String RECORD = "1={ignore1=Value, entity={ignore2='Another value'}, important='This is the expected result'}";
	private static final String EXPECTED_RESULT = "This is the expected result";
	
	@Test
	public void test() {
		final CGTextDecoder decoder = new CGTextDecoder();
		final LiteralExtractor extractor = new LiteralExtractor("important");
		final ObjectBuffer<String> buffer = new ObjectBuffer<String>(1);
		
		decoder.setReceiver(extractor).setReceiver(buffer);
		
		decoder.process(RECORD);
		decoder.closeStream();
		
		Assert.assertEquals(EXPECTED_RESULT, buffer.pop());
	}

}
