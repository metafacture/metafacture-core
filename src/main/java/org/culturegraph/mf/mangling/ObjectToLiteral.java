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
package org.culturegraph.mf.mangling;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Outputs a record containing the input object as literal.
 *
 * @param <T> input object type
 * @author Christoph Böhme
 */
@Description("Outputs a record containing the input object as literal")
@Out(StreamReceiver.class)
@FluxCommand("object-to-literal")
public final class ObjectToLiteral<T> extends
		DefaultObjectPipe<T, StreamReceiver> {

	public static final String DEFAULT_LITERAL_NAME = "obj";

	private String literalName = DEFAULT_LITERAL_NAME;

	public void setLiteralName(final String literalName) {
		this.literalName = literalName;
	}

	public String getLiteralName() {
		return literalName;
	}

	@Override
	public void process(final T obj) {
		assert obj!=null;
		assert !isClosed();
		getReceiver().startRecord("");
		getReceiver().literal(literalName, obj.toString());
		getReceiver().endRecord();
	}

}
