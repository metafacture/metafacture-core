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
package org.culturegraph.mf.stream.converter.bib;

import java.util.Collections;
import java.util.List;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.DataFilePath;
import org.culturegraph.mf.stream.converter.LineReader;
import org.culturegraph.mf.stream.converter.bib.MissingIdException;
import org.culturegraph.mf.stream.converter.bib.PicaDecoder;
import org.culturegraph.mf.stream.sink.Counter;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.types.Event;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests {@link PicaDecoder}. So far only verifies that the correct number of
 * records and fields is read.
 * 
 * @author Markus Michael Geipel
 * @see PicaDecoder
 */
public final class SimplePicaReaderTest {

	private static final int NUM_RECORDS = 11;
	private static final int NUM_LITERALS = 289;

	private final ResourceOpener opener = new ResourceOpener();
	private final LineReader lineReader = new LineReader();
	private final PicaDecoder picaDecoder = new PicaDecoder();
	private final Counter countStreamReceiver = new Counter();

	@Test(expected=MissingIdException.class)
	public void testCorruptRead() {
		opener.setReceiver(lineReader)
				.setReceiver(picaDecoder)
				.setReceiver(countStreamReceiver);

		opener.process(DataFilePath.PND_PICA);
		picaDecoder.process("!THIS IS A CORRUPT RECORD!");
		opener.closeStream();
	}
	
	@Test
	public void testRead() {
		opener.setReceiver(lineReader)
		.setReceiver(picaDecoder)
		.setReceiver(countStreamReceiver);

		opener.process(DataFilePath.PND_PICA);
		// record contains empty fields (should be skipped): 
		picaDecoder.process("\u001e\u001e003@ \u001f012235" + "\u001e\u001e");
		opener.closeStream();
		
		Assert.assertEquals("Number of records is incorrect", NUM_RECORDS,
				countStreamReceiver.getNumRecords());
		Assert.assertEquals("Number of literals is incorrect", NUM_LITERALS,
				countStreamReceiver.getNumLiterals());
	}
	
	@Test
	public void testSkipEmptyStrings() {
		final PicaDecoder decoder = new PicaDecoder();
		
		final List<Event> expected = Collections.emptyList();
		final StreamValidator validator = new StreamValidator(expected);
		
		decoder.setReceiver(validator);
		
		try {
			decoder.process(" ");
			decoder.closeStream();
		} catch (FormatException e) {
			Assert.fail(e.toString());
		}
	}
}
