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
package org.culturegraph.mf.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Processes input from a reader line by line.
 *
 * @author Christoph Böhme
 *
 */
@Description("Processes input from a reader line by line.")
@In(Reader.class)
@Out(String.class)
@FluxCommand("as-lines")
public final class LineReader
		extends DefaultObjectPipe<Reader, ObjectReceiver<String>> {
	private static final int BUFFER_SIZE = 1024 * 1024 * 16;

	@Override
	public void process(final Reader reader) {
		assert !isClosed();
		assert null!=reader;
		process(reader, getReceiver());
	}

	public static void process(final Reader reader, final ObjectReceiver<String> receiver) {
		final BufferedReader lineReader = new BufferedReader(reader, BUFFER_SIZE);
		try {
			String line = lineReader.readLine();
			while (line != null) {
				receiver.process(line);
				line = lineReader.readLine();
			}
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

}
