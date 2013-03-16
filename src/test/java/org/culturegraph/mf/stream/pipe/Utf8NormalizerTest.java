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

import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests {@link Utf8Normalizer}.
 * 
 * @author Christoph Böhme
 *
 */
public final class Utf8NormalizerTest {

	private static final String STRING_WITH_DIACRITICS = 
			"Bauer, Sigmund: Über den Einfluß der Ackergeräthe auf den Reinertrag.";
	
	private static final String STRING_WITH_PRECOMPOSED_CHARS = 
			"Bauer, Sigmund: Über den Einfluß der Ackergeräthe auf den Reinertrag.";
	
	private Utf8Normalizer normalizer;
	
	@Mock
	private ObjectReceiver<String> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		normalizer = new Utf8Normalizer();
		normalizer.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		normalizer.closeStream();
	}
	
	@Test
	public void testShouldReplaceDiacriticsWithPrecomposedChars() {		
		normalizer.process(STRING_WITH_DIACRITICS);
		
		verify(receiver).process(STRING_WITH_PRECOMPOSED_CHARS);
	}

}
