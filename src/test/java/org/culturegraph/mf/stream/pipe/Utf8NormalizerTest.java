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

import org.culturegraph.mf.framework.DefaultObjectReceiver;
import org.junit.Test;

/**
 * Tests {@link Utf8Normalizer}.
 * 
 * @author Christoph Böhme
 *
 */
public final class Utf8NormalizerTest {

	// The umlauts in this string are composed of two characters (u and ", e.g.):
	private static final String INPUT_STR = 
			"Bauer, Sigmund: Über den Einfluß der Ackergeräthe auf den Reinertrag.";
	
	// The umlauts in this string are individual characters:
	private static final String OUTPUT_STR = 
			"Bauer, Sigmund: Über den Einfluß der Ackergeräthe auf den Reinertrag.";
	
	@Test
	public void testNormalization() {
		final Utf8Normalizer normalizer = new Utf8Normalizer();
		
		normalizer.setReceiver(new DefaultObjectReceiver<String>() {
			@Override
			public void process(final String obj) {
				assertEquals(OUTPUT_STR, obj);
			}
		});
		
		normalizer.process(INPUT_STR);
	}

}
