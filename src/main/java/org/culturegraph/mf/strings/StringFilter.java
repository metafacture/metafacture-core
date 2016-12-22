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
 * Only forwards records which match (or do not match) a regular expression
 * given in the constructor.
 *
 * @author Christoph Böhme
 *
 */
@Description("Only forwards records which match (or do not match) a regular expression given in the constructor")
@In(String.class)
@Out(String.class)
@FluxCommand("filter-strings")
public final class StringFilter extends
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	private final Matcher matcher;
	private boolean passMatches=true;

	public StringFilter(final String pattern) {
		this.matcher = Pattern.compile(pattern).matcher("");
	}

	public String getPattern() {
		return matcher.pattern().pattern();
	}

	public boolean isPassMatches() {
		return passMatches;
	}

	public void setPassMatches(final boolean passMatches) {
		this.passMatches = passMatches;
	}

	@Override
	public void process(final String obj) {
		assert !isClosed();
		assert null!=obj;
		matcher.reset(obj);
		if (matcher.find() == passMatches) {
			getReceiver().process(obj);
		}
	}

}
