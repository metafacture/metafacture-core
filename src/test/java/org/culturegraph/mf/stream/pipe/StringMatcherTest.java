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

import junit.framework.Assert;

import org.culturegraph.mf.stream.pipe.ObjectBuffer;
import org.culturegraph.mf.stream.pipe.StringMatcher;
import org.junit.Test;


/**
 * @author Christoph Böhme
 *
 */
public final class StringMatcherTest {

	@Test
	public void testMatcher() {
		final StringMatcher matcher = new StringMatcher();
		final ObjectBuffer<String> result = new ObjectBuffer<String>();
		
		matcher.setReceiver(result);
		
		matcher.setPattern("PLACEHOLDER");
		matcher.setReplacement("Karl");
		matcher.process("Hi PLACEHOLDER! -- Bye PLACEHOLDER!");
		
		matcher.setPattern("^([^ ]+) .*$");
		matcher.setReplacement("$1");
		matcher.process("important-bit this can be ignored");
		
		matcher.closeStream();
		
		Assert.assertEquals("Hi Karl! -- Bye Karl!", result.pop());
		Assert.assertEquals("important-bit", result.pop());
		Assert.assertNull(result.pop());
	}

}
