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

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;

import org.culturegraph.mf.morph.DataFilePath;
import org.culturegraph.mf.stream.reader.PicaReader;
import org.culturegraph.mf.stream.reader.Reader;
import org.culturegraph.mf.stream.sink.Counter;
import org.junit.Test;


/**
 * Tests {@link Splitter}.
 * 
 * @author Markus Michael Geipel
 * 
 */
public final class SplitterTest {

	private static final int NUM_TP_RECORDS = 3;
	private static final int NUM_TN_RECORDS = 7;

	@Test
	public void testCorrectTeeFunction() throws IOException {
		final Reader picaReader = new PicaReader();

		final Splitter splitter = new Splitter("morph/typeSplitter.xml");

		final Counter countingWriterTp = new Counter();
		final Counter countingWriterTn = new Counter();

		picaReader.setReceiver(splitter).setReceiver("Tn", countingWriterTn);

		splitter.setReceiver("Tp", countingWriterTp);

		picaReader.process(new FileReader(DataFilePath.PND_PICA));

		assertEquals(NUM_TN_RECORDS, countingWriterTn.getNumRecords());
		assertEquals(NUM_TP_RECORDS, countingWriterTp.getNumRecords());
	}
	
}
