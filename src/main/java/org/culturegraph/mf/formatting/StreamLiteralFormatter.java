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
package org.culturegraph.mf.formatting;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Emits the name and value of each literal which is received as a string.
 * Name and value are separated by a separator string. The default separator
 * string is a tab. If a literal name is empty, only the value will be output
 * without a separator.
 * <p>
 * The module ignores record and entity events. This means that literal names
 * are not prefixed by the name of the entity which contains them.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("Outputs the name and value of each literal which is received " +
		"as a string. Name and value are separated by a separator " +
		"string. The default separator string is a tab. If a literal " +
		"name is empty, only the value will be output without a separator. " +
		"The module ignores record and entity events. In particular, " +
		"this means that literal names are not prefixed by the name " +
		"of the entity which contains them.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-literals")
public final class StreamLiteralFormatter
		extends DefaultStreamPipe<ObjectReceiver<String>> {

	/**
	 * The default value for {@link #setSeparator(String)}.
	 */
	public static final String DEFAULT_SEPARATOR = "\t";

	private String separator = DEFAULT_SEPARATOR;

	/**
	 * Sets the separator between the literal name and value. The separator is
	 * only added if the literal name is not empty.
	 * <p>
	 * The default separator is &ldquo;{@link #DEFAULT_SEPARATOR}&rdquo;.
	 * <p>
	 * The parameter can be changed at any time during processing. It becomes
	 * effective with the next literal received.
	 *
	 * @param separator the separator string.
	 */
	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	public String getSeparator() {
		return separator;
	}

	@Override
	public void literal(final String name, final String value) {
		if (name == null || name.isEmpty()) {
			getReceiver().process(value);
		} else {
			getReceiver().process(name + separator + value);
		}
	}

}
