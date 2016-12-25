/*
 * Copyright 2016 hbz
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
package org.culturegraph.mf.mangling;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.ForwardingStreamPipe;

/**
 * Replaces null values with replacement string, or, if replacement
 * string is null (default), discards null values entirely.
 *
 * @author Jens Wille
 *
 */
@Description("Discards or replaces null values")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class NullFilter extends ForwardingStreamPipe {

	private String replacement = null;

	public void setReplacement(final String replacement) {
		this.replacement = replacement;
	}

	public String getReplacement() {
		return replacement;
	}

	@Override
	public void literal(final String name, final String value) {
		if (value != null) {
			getReceiver().literal(name, value);
		} else if (replacement != null) {
			getReceiver().literal(name, replacement);
		}
	}

}
