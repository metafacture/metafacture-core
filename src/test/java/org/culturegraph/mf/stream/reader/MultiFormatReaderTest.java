/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.reader;

import org.culturegraph.mf.stream.reader.MultiFormatReader;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests {@link MultiFormatReader}
 * @author Markus Michael Geipel
 *
 */
public final class MultiFormatReaderTest {
	
	private static final String PICA = "pica";
	private static final String MAB2 = "mab2";
	

	@Test(expected=IllegalStateException.class)
	public void testMissingFormat(){
		final MultiFormatReader formatReader = new MultiFormatReader();
		formatReader.read("gurk");
	}
	
	@Test
	public void testFormatSwitch(){
		final MultiFormatReader formatReader = new MultiFormatReader();
		formatReader.setFormat(PICA);
		Assert.assertEquals(PICA, formatReader.getFormat());
		formatReader.setFormat(MAB2);
		Assert.assertEquals(MAB2, formatReader.getFormat());
		
	}
	
	@Test(expected=IllegalStateException.class)
	public void testMissingReceiver(){	
		final MultiFormatReader formatReader = new MultiFormatReader();
		formatReader.setFormat(PICA);
		formatReader.read("hula");
	}
	

}
