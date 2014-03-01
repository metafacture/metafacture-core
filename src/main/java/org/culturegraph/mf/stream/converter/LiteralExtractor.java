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
package org.culturegraph.mf.stream.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * @author Christoph Böhme
 *
 */
@Description("Extracts literal values from a stream.")
@In(StreamReceiver.class)
@Out(String.class)
public final class LiteralExtractor extends DefaultStreamPipe<ObjectReceiver<String>>  {

	private Matcher matcher; 
	
	public LiteralExtractor() {
		this(".*");
	}
	
	public LiteralExtractor(final String pattern) {
		super();
		
		matcher = Pattern.compile(pattern).matcher("");
	}
	
	public String getPattern() {
		return matcher.pattern().pattern();
	}

	public void setPattern(final String pattern) {
		this.matcher = Pattern.compile(pattern).matcher("");
	}
	
	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		matcher.reset(name);
		if (matcher.matches()) {
			getReceiver().process(value);
		}
	}
	
}
