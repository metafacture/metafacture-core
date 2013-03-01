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

import org.culturegraph.mf.stream.pipe.StreamLogger;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Tests if {@link StreamLogger} is correctly piping all events.
 * 
 * @author Markus Michael Geipel
 */

public final class StreamLoggerTest {

	@Test
	@Ignore
	public void testCorrectPipeFunction() {
//		final ResourceOpener opener = new ResourceOpener();
//		final PicaDecoder picaDecoder = new PicaDecoder();
//		final StringWriter referenceWriter = new StringWriter();
//				
//		opener.setReceiver(new LineReader())
//				.setReceiver(picaDecoder)
//				.setReceiver(new StreamWriter(referenceWriter));
//
//		opener.process(DataFilePath.PND_PICA);
//		opener.closeStream();
//
//		final StringWriter finalWriter = new StringWriter();
//
//		picaDecoder.setReceiver(new StreamLogger("test-logger"))
//				.setReceiver(new StreamWriter(finalWriter));
//
//		opener.process(DataFilePath.PND_PICA);
//		opener.closeStream();
//
//		Assert.assertEquals(referenceWriter.toString(), finalWriter.toString());
//
	}
}
