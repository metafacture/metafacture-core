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

import java.util.regex.Pattern;


/**
 * @author Markus Michael Geipel
 */
public final class Replace extends AbstractSimpleStatelessFunction {

	private Pattern pattern;
	private String with;
	
	@Override
	public String process(final String value) {
		return pattern.matcher(value).replaceAll(with);
	}

	/**
	 * @param string the string to set
	 */
	public void setPattern(final String string) {
		this.pattern = Pattern.compile(string);
	}

	/**
	 * @param with the with to set
	 */
	public void setWith(final String with) {
		this.with = with;
	}

}
