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

import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link PicaEncoder}
 * 
 * @author li
 *
 */
public final class PicaEncoderTest {

	private PicaEncoder picaEncoder;
	
	@Mock
	private ObjectReceiver<String> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		picaEncoder = new PicaEncoder();
		picaEncoder.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		picaEncoder.closeStream();
	}
	
	@Test
	public void testShouldWriteFieldAndSubfield() {
		picaEncoder.startRecord("17709958X");
		picaEncoder.startEntity("003@");
		picaEncoder.literal("0", "17709958X");
		picaEncoder.endEntity();
		picaEncoder.startEntity("028@");
		picaEncoder.literal("P", "Abläöübolo");
		picaEncoder.literal("n", "VIX");
		picaEncoder.literal("l", "Bapst");
		picaEncoder.endEntity();
		picaEncoder.endRecord();
		
		verify(receiver).process("003@ \u001f017709958X\u001e028@ \u001fPAbla\u0308o\u0308u\u0308bolo\u001fnVIX\u001flBapst\u001e");
	}
	
//	@Test
//	public void testShouldPrefer007KOverRecordId() {
//		picaEncoder.startRecord("10");
//		picaEncoder.startEntity("007");
//		picaEncoder.literal("K", "11");
//		picaEncoder.endEntity();
//		picaEncoder.endRecord();
//		
//		verify(receiver).process("007K11");
//	}
	
}
