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
package org.culturegraph.mf.metamorph.functions;

import org.culturegraph.mf.metamorph.api.helpers.AbstractSimpleStatelessFunction;

/**
 * Extracts a substring from the received value.
 *
 * @author Markus Michael Geipel
 */
public final class Substring extends AbstractSimpleStatelessFunction {

	private int start;
	private int end;

	@Override
	public String process(final String value) {
		final int length = value.length();

		if (start > length - 1) {
			return null;
		}
		final int adjEnd;
		if (end == 0  || end > length) {
			adjEnd = length;
		} else {
			adjEnd = end;
		}

		return value.substring(start, adjEnd);
	}

	/**
	 * @param start
	 *            start of substring
	 */
	public void setStart(final String start) {
		this.start = Integer.parseInt(start);
	}

	/**
	 * @param end end of substring, if end==0 the the complete
	 *            remaining string is returned
	 *
	 */
	public void setEnd(final String end) {
		this.end = Integer.parseInt(end);
	}

}
