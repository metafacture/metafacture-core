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
package org.culturegraph.mf.stream.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Decodes an incoming string based on a regular expression using
 * named-capturing groups denoted by (?&lt;name&gt;X). The name of a matched
 * group ("name") is used as the literal name, the captured content ("X") as
 * the literal value. Group names are composed of the following characters:
 * <dl>
 * <li>The uppercase letters 'A' through 'Z' ('\u0041' through '\u005a'),</li>
 * <li>The lowercase letters 'a' through 'z' ('\u0061' through '\u007a'),</li>
 * <li>The digits '0' through '9' ('\u0030' through '\u0039').</li>
 * </dl>
 * 
 * The first character of a group name must be a letter.
 * </p>
 * <p>
 * The regular expression must not contain any non-named capture groups.
 * </p>
 * 
 * <p>
 * Example: The regex
 * 
 * <blockquote>
 * 
 * <pre>
 * abc(?&lt;foo&gt;[0-9]+)def(?&lt;bar&gt;[x-z]+)ghi
 * </pre>
 * 
 * </blockquote>
 * 
 * matched against the input <blockquote>
 * 
 * <pre>
 * abc42defxyzzyghi
 * </pre>
 * 
 * </blockquote> will produce the sequence <blockquote>
 * 
 * <pre>
 * startRecord(null);
 * literal(&quot;foo&quot;, &quot;42&quot;);
 * literal(&quot;bar&quot;, &quot;xyzzy&quot;);
 * endRecord();
 * </pre>
 * 
 * </blockquote>
 * </p>
 * 
 * @author Thomas Seidel
 * 
 */
@Description("Decodes an incoming string based on a regular expression using named-capturing groups")
@In(String.class)
@Out(StreamReceiver.class)
public final class RegexDecoder extends DefaultObjectPipe<String, StreamReceiver> {

	public static final String ID_CAPTURE_GROUP = "id";
	
	private static final Logger LOG = LoggerFactory
			.getLogger(RegexDecoder.class);

	private static final Pattern NAMED_CAPTURE_GROUP_PATTERN = Pattern
			.compile("\\(\\?<([A-Za-z0-9]+)>");

	private final Matcher matcher;
	private final List<String> captureGroupNames = new ArrayList<String>();

	private String defaultLiteralName;

	public RegexDecoder(final String regex) {
		super();
		
		final Matcher namedCaptureGroupMatcher = NAMED_CAPTURE_GROUP_PATTERN
				.matcher(regex);
		while (namedCaptureGroupMatcher.find()) {
			final String captureGroupName = namedCaptureGroupMatcher.group(1);
			LOG.debug("captureGroupName is: {}", captureGroupName);
			captureGroupNames.add(captureGroupName);
		}
		final String java6regex = namedCaptureGroupMatcher.replaceAll("(");
		LOG.debug("Cleaned java6regex is: {}", java6regex);
		
		matcher = Pattern.compile(java6regex).matcher("");
	}

	public String getDefaultLiteralName() {
		return this.defaultLiteralName;
	}
	
	public void setDefaultLiteralName(final String defaultLiteralName) {
		this.defaultLiteralName = defaultLiteralName;
	}

	@Override
	public void process(final String string) {
		assert !isClosed();
		matcher.reset(string);

		final String id;
		final int groupIndex = captureGroupNames.indexOf(ID_CAPTURE_GROUP) + 1;
		if (groupIndex > 0 && matcher.find()) {
			id = matcher.group(groupIndex);
		} else {
			id = "";
		}
		getReceiver().startRecord(id);
		
		if (defaultLiteralName != null) {
			getReceiver().literal(defaultLiteralName, string);
		}
		
		matcher.reset();
		while (matcher.find()) {
			final int groupCount = matcher.groupCount();
			LOG.debug("groupCount() is: {}", Integer.valueOf(groupCount));
			for (int group = 1; group <= groupCount; ++group) {
				final String literalName = captureGroupNames.get(group - 1);
				final String literalValue = matcher.group(group);
				LOG.debug("group# is: {}, literalName is: {}, literalValue is: {}", 
						Integer.valueOf(group), literalName, literalValue);
				getReceiver().literal(literalName, literalValue);
			}
		}
		
		getReceiver().endRecord();
	}

}
