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

import org.junit.Test;


/**
 * tests {@link SetReplacer}
 *
 * @author Markus Michael Geipel
 *
 */
public final class SetReplaceTest {

	@Test
	public void testReplace() {

		final SetReplacer setReplace = new SetReplacer();
		final String text = "auf sylt mit super krabben entsafter und apfel";
		final String target = "auf hawaii mit Mai Tai und surfboard";

		setReplace.addReplacement("sylt", "hawaii");
		setReplace.addReplacement("apfel", "surfboard");
		setReplace.addReplacement("krabben", "shirt");
		setReplace.addReplacement("super krabben entsafter", "Mai Tai");

		assertEquals(target, setReplace.replaceIn(text));
	}

	@Test
	public void testReplaceWithInclusion() {

		final SetReplacer setReplace = new SetReplacer();


		setReplace.addReplacement("fünf", "5");
		setReplace.addReplacement("fünfzig", "50");

		assertEquals("50 äpfel", setReplace.replaceIn("fünfzig äpfel"));
		assertEquals("5 äpfel", setReplace.replaceIn("fünf äpfel"));
	}

}
