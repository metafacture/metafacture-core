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
import org.culturegraph.mf.stream.pipe.IdentityStreamPipe;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Christoph Böhme
 *
 */
public final class IdentityStreamPipeTest {
	
	private static final String INPUT1 = "1={lit1=C, ent1={ent1lit1=X}}";
	private static final String INPUT2 = "2={lit2=D}";

	@Test
	public void test() {
		final CGTextDecoder decoder = new CGTextDecoder();
		final EventList buffer = new EventList();
		
		decoder.setReceiver(buffer);
		
		decoder.process(INPUT1);
		decoder.process(INPUT2);
		decoder.closeStream();
		
		final IdentityStreamPipe identityPipe = new IdentityStreamPipe();
		final StreamValidator validator = new StreamValidator(buffer.getEvents());
		
		decoder.setReceiver(identityPipe).setReceiver(validator);

		try {
			decoder.process(INPUT1);
			decoder.process(INPUT2);
			decoder.closeStream();
		} catch(FormatException e) {
			Assert.fail(e.toString());
		}

	}

}
