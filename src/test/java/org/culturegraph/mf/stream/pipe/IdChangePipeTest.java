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
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Assert;
import org.junit.Test;


/**
 * tests {@link IdChangePipe}.
 * 
 * @author Christoph Böhme
 *
 */
public final class IdChangePipeTest {

	private static final String RECORD3 = "3={ name=test }";
	
	@Test
	public void test() {
		
		final CGTextDecoder decoder = new CGTextDecoder();
		
		final EventList expected = new EventList();
		
		decoder.setReceiver(expected);
		
		decoder.process("1={ name=test, _id=1, entity={ name=test } }");
		decoder.process("2={ name=test, _id=2}");
		//decoder.process(RECORD3);
		//decoder.closeStream();
				
		final IdChangePipe idChangePipe = new IdChangePipe();
		idChangePipe.setKeepIdless(false);
		final StreamValidator validator = new StreamValidator(expected.getEvents());
		
		decoder.setReceiver(idChangePipe).setReceiver(validator);

		try {
			decoder.process("one={ name=test, _id=1, entity={ name=test } }");
			decoder.process("two={ name=test, _id=2}");
			decoder.process(RECORD3);
			decoder.closeStream();
		} catch(FormatException e) {
			Assert.fail(e.toString());
		}
	}

}
