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
package org.culturegraph.mf.morph.functions;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses the received value as a key for looking up
 * the output value in a map. The maps keys may be regexes.
 * The value to look up will be tried to match with each regex-key. 
 * If none matches, the default value is returned, if a default was set.
 *
 * @author Jan Polowinski
 */
public final class RegexLookup extends AbstractLookup {

	private String defaultValue;
	private boolean prepared;
	// Problem: TreeMap is sorted, but the map we receive is not ... so the values are already potentially in random order
	// That means you cannot yet rely on the order in case you have overlapping regexes!
	private Map<String, String> regexMap = new TreeMap<String,String>();

	
	public void setDefault(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String process(final String text) {
		if (!prepared) {
			addReplacements(getMap());
			prepared = true;
		}
		final String returnValue = replaceIn(text);

		if (returnValue == null) {
			return defaultValue;
		}
		return returnValue;
	}

	public void setIn(final String mapName) {
		setMap(mapName);
	}
	
	public String replaceIn(final String text) {
		
		String latestMatch = null;

		if (null==regexMap) System.out.println("regexmap was null");
		if (null!=regexMap && regexMap.isEmpty()) System.out.println("regexmap was empty");
		if (null!=regexMap) System.out.println(regexMap);
		
		for (Map.Entry<String, String> entry : regexMap.entrySet())
		{
			final String regexKey = entry.getKey();
			final String replaceValue = entry.getValue();
			final Matcher matcher;
			
			// TODO reuse compiled regexes
			Pattern pattern = Pattern.compile(regexKey);
			matcher = pattern.matcher(text);
			if (matcher.matches()) {
				latestMatch = replaceValue;
				// for now break here, since we cannot handle multiple regex matches anyway
				break;
			}
		}
		
		return latestMatch;
	}
	
	public void addReplacement(final String key, final String with) {
		regexMap.put(key, with);
	}

	public void addReplacements(final Map<String, String> replacements) {

		for (Entry<String, String> entry : replacements.entrySet()) {
			addReplacement(entry.getKey(), entry.getValue());
		}
	}

}
