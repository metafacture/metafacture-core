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
package org.culturegraph.mf.morph.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.MorphException;
import org.culturegraph.mf.morph.NamedValueSource;
import org.culturegraph.mf.util.StringUtil;

/**
 * Performs regexp matching
 * 
 * @author Markus Michael Geipel
 */
public final class Regexp extends AbstractFunction {

	// private static final String TRUE = "true";
	private Matcher matcher;
	private String format;
	private final Map<String, String> tempVars = new HashMap<String, String>();

	@Override
	public void receive(final String name, final String value, final NamedValueSource source, final int recordCount,
			final int entityCount) {
		matcher.reset(value);
		if (null == format) {
			while (matcher.find()) {
				final String group = matcher.group();
				if (!group.isEmpty()) {
					getNamedValueReceiver().receive(name, group, source, recordCount, entityCount);
				}
			}
		} else {
			while (matcher.find()) {
				populateVars();
				if (!tempVars.isEmpty()) {
					getNamedValueReceiver().receive(name,
							StringUtil.format(format, tempVars), source, recordCount, entityCount);
				}
			}
		}
	}

	private void populateVars() {
		tempVars.clear();
		for (int i = 0; i <= matcher.groupCount(); ++i) {
			if (!matcher.group(i).isEmpty()) {
				tempVars.put(String.valueOf(i), matcher.group(i));
			}
		}
	}

	/**
	 * @param match
	 *            the match to set
	 */
	public void setMatch(final String match) {
		this.matcher = Pattern.compile(match).matcher("");
	}

	/**
	 * @param format
	 *            the output to set
	 */
	public void setFormat(final String format) {
		this.format = format;
	}

	/**
	 * Thrown if no match was found
	 * 
	 */
	public static final class PatternNotFoundException extends MorphException {
		private static final long serialVersionUID = 4113458605196557204L;

		public PatternNotFoundException(final Pattern pattern, final String input) {
			super("Pattern '" + pattern + "' not found in '" + input + "'");
		}
	}

}
