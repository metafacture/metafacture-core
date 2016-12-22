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
package org.culturegraph.mf.strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;


/**
 * Matches the incoming strings against a regular expression and replaces
 * the matching parts.
 *
 * @author Christoph Böhme
 */
@Description("Matches the incoming strings against a regular expression and replaces the matching parts.")
@In(String.class)
@Out(String.class)
@FluxCommand("match")
public final class StringMatcher extends
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	private Matcher matcher;
	private String replacement;

	public String getPattern() {
		return matcher.pattern().pattern();
	}

	public void setPattern(final String pattern) {
		this.matcher = Pattern.compile(pattern).matcher("");
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(final String replacement) {
		this.replacement = replacement;
	}

	@Override
	public void process(final String obj) {
		assert !isClosed();
		assert null!=obj;
		matcher.reset(obj);
		getReceiver().process(matcher.replaceAll(replacement));
	}

}
