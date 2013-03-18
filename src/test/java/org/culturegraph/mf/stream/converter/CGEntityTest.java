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

import org.culturegraph.mf.stream.converter.CGEntityDecoder;
import org.culturegraph.mf.stream.converter.CGEntityEncoder;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests correctness of {@link CGEntityDecoder} and {@link CGEntityEncoder}.
 * 
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
public final class CGEntityTest {
	
	
	@Test
	@Ignore
	public void testReadWriteRead(){
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
//		final CGEntityEncoder cgEntityEnoder = new CGEntityEncoder();
//		final StringWriter tempWriter = new StringWriter();
//			
//		picaDecoder.setReceiver(cgEntityEnoder)
//				.setReceiver(new ObjectWriter<String>(tempWriter));
//		
//		opener.process(DataFilePath.PND_PICA);
//		opener.closeStream();
//		
//		final LineSplitter lineSplitter = new LineSplitter();
//		final StringWriter finalWriter = new StringWriter();
//		
//		lineSplitter.setReceiver(new CGEntityDecoder())
//				.setReceiver(new StreamWriter(finalWriter));
//		
//		lineSplitter.process(tempWriter.toString());
//		lineSplitter.closeStream();
//		
//		Assert.assertEquals(referenceWriter.toString(), finalWriter.toString());
	}
	
}
