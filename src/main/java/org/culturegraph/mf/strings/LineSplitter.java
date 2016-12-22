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

import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Splits a string at new lines and sends each line to the receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Splits a string at new lines and sends each line to the receiver.")
@In(String.class)
@Out(String.class)
@FluxCommand("split-lines")
public final class LineSplitter
		extends DefaultObjectPipe<String, ObjectReceiver<String>> {

	private static final char NEWLINE = '\n';
	private static final Pattern LINE_PATTERN = Pattern.compile(
			String.valueOf(NEWLINE), Pattern.LITERAL);

	@Override
	public void process(final String lines) {
		assert !isClosed();
		for (final String record : LINE_PATTERN.split(lines)) {
			getReceiver().process(record);
		}
	}

}
