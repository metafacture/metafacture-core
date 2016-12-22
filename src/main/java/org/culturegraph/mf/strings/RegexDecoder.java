/*
 * Copyright 2016 Christoph Böhme
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;


/**
 * Decodes a string based on a regular expression using named capture groups.
 * <p>
 * Named capture groups are denoted by {@literal (?<name>X)}. The name of a
 * matched group (&quot; name&quot;) is used as the literal name, the
 * captured content (&quot; X&quot;) as the literal value. Group names are
 * composed of the following characters:
 * <ul>
 *   <li>The uppercase letters 'A' through 'Z',
 *   <li>The lowercase letters 'a' through 'z',
 *   <li>The digits '0' through '9'.
 * </ul>
 * The first character of a group name must be a letter.
 * <p>
 * The pattern is matched repeatedly to the input string. On each match the
 * captured content of all named capture groups in the pattern is emitted as
 * literals. Non-matching parts of the input are ignored.
 * <p>
 * The regular expression may contain unnamed capture groups. These are
 * ignored.
 * <p>
 * If the pattern contains a capture group named &quot;{@value
 * #ID_CAPTURE_GROUP}&quot;, the first match of this group will be used as
 * record identifier. If there is no such capture group or if it does not
 * match, the empty string is used as record identifier.
 * <p>
 * Example: The regex
 * <pre>{@literal
 * a=(?<foo>[0-9]+),b=(?<bar>[x-z]+)
 * }</pre>
 * matched against the input
 * <pre>{@literal
 * a=42,b=xyzzy,c=ignored,a=23,b=xyz,d=ignored
 * }</pre>
 * will produce the sequence of events:
 * <pre>{@literal
 * start-record ""
 * literal "foo": 42
 * literal "bar": xyzzy
 * literal "foo": 23
 * literal "bar": xyz
 * end-record
 * }</pre>
 *
 * @author Thomas Seidel
 * @author Christoph Böhme
 *
 */
@Description("Decodes a string based on a regular expression using named capture groups")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("regex-decode")
public final class RegexDecoder extends DefaultObjectPipe<String, StreamReceiver> {

	public static final String ID_CAPTURE_GROUP = "id";

	private static final Pattern NAMED_CAPTURE_GROUP_PATTERN =
			Pattern.compile("\\(\\?<([A-Za-z0-9]+)>");

	private final Matcher matcher;
	private final List<String> captureGroupNames;
	private final boolean hasRecordIdCaptureGroup;

	private String rawInputLiteral;

	public RegexDecoder(final String regex) {
		matcher = Pattern.compile(regex).matcher("");
		captureGroupNames = collectCaptureGroupNames(regex);
		hasRecordIdCaptureGroup = captureGroupNames.contains(ID_CAPTURE_GROUP);
	}

	private List<String> collectCaptureGroupNames(final String regex) {
		final List<String> groupNames = new ArrayList<>();
		final Matcher groupNameMatcher = NAMED_CAPTURE_GROUP_PATTERN.matcher(regex);
		while (groupNameMatcher.find()) {
			groupNames.add(groupNameMatcher.group(1));
		}
		return groupNames;
	}

	/**
	 * Sets the name of a literal containing the unmodified input received by
	 * {@link RegexDecoder}. If not set, no raw input literals are emitted.
	 * <p>
	 * The raw input <i>literal</i> event is always the first event emitted
	 * after the <i>start-record</i> event.
	 * <p>
	 * This parameter can be changed at any time during processing. It becomes
	 * effective with the next record being processed.
	 *
	 * @param rawInputLiteral name of the literal which contains the umodified
	 *                        input string. If null, raw input literals will be
	 *                        disabled.
	 */
	public void setRawInputLiteral(final String rawInputLiteral) {
		this.rawInputLiteral = rawInputLiteral;
	}

	public String getRawInputLiteral() {
		return this.rawInputLiteral;
	}

	@Override
	public void process(final String input) {
		matcher.reset(input);
		if (!matcher.find()) {
			return;
		}
		getReceiver().startRecord(getRecordId());
		emitRawInputLiteral(input);
		emitCaptureGroupsAsLiterals();
		getReceiver().endRecord();
	}

	private String getRecordId() {
		if (!hasRecordIdCaptureGroup) {
			return "";
		}
		return matcher.group(ID_CAPTURE_GROUP);
	}

	private void emitCaptureGroupsAsLiterals() {
		do {
			for (final String groupName : captureGroupNames) {
				getReceiver().literal(groupName, matcher.group(groupName));
			}
		} while (matcher.find());
	}

	private void emitRawInputLiteral(final String input) {
		if (rawInputLiteral != null) {
			getReceiver().literal(rawInputLiteral, input);
		}
	}

}
