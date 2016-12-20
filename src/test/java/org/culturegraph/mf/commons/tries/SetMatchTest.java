/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.commons.tries;

import static org.junit.Assert.assertEquals;

import org.culturegraph.mf.commons.tries.SetMatcher.Match;
import org.junit.Test;

/**
 * tests {@link SetMatcher}
 *
 * @author Markus Michael Geipel
 *
 */
public final class SetMatchTest {

	@Test
	public void testSetMatch() {
		final SetMatcher<String> setMatch = new SetMatcher<String>();

		final String[] cities = { "Perth", "York", "York Town", "München", "New York City", "New York", "Petersburg",
				"ert", };
		final int[] matches = { 7, 0, 7, 5, 1, 4, 1, 1, 2, 3 };
		final String text = "Pexrt Perth Peerth New York City York York Town München";

		for (int i = 0; i < cities.length; ++i) {
			final String city = cities[i];
			setMatch.put(city, city);
		}
		int index = 0;

		//System.out.println(text);
		for (Match<String> match : setMatch.match(text)) {
			//System.out.println(match.getValue() + " " + match.getStart());
			assertEquals(cities[matches[index]], match.getValue());
			++index;
		}
		// setMatch.printDebug(System.err);

		assertEquals("missing matches", matches.length, index);
	}

}
