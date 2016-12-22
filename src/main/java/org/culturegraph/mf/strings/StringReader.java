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

import java.io.Reader;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;


/**
 * Creates a reader for the supplied string and sends it to the receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Creates a reader for the supplied string and sends it to the receiver")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("read-string")
public final class StringReader
		extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

	@Override
	public void process(final String str) {
		getReceiver().process(new java.io.StringReader(str));
	}

}
