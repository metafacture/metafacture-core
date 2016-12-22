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
package org.culturegraph.mf.io;

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
 * <p>Reads data from a {@code Reader} and splits it into individual
 * records.</p>
 *
 * <p>The default separator is the global separator character (0x001d).
 * Empty records are skipped by default.</p>
 *
 * @author Christoph Böhme
 *
 */
@Description("Reads data from a Reader and splits it into individual records")
@In(Reader.class)
@Out(String.class)
@FluxCommand("as-records")
public final class RecordReader extends
		DefaultObjectPipe<Reader, ObjectReceiver<String>> {

	public static final char DEFAULT_SEPARATOR = '\u001d';

	private static final int BUFFER_SIZE = 1024 * 1024 * 16;

	private final StringBuilder builder = new StringBuilder();
	private final char[] buffer = new char[BUFFER_SIZE];

	private char separator = DEFAULT_SEPARATOR;
	private boolean skipEmptyRecords = true;

	public void setSeparator(final String separator) {
		if (separator.length() >= 1) {
			this.separator = separator.charAt(0);
		} else {
			this.separator = DEFAULT_SEPARATOR;
		}
	}

	public void setSeparator(final char separator) {
		this.separator = separator;
	}

	public char getSeparator() {
		return separator;
	}

	public void setSkipEmptyRecords(final boolean skipEmptyRecords) {
		this.skipEmptyRecords = skipEmptyRecords;
	}

	public boolean getSkipEmptyRecords() {
		return skipEmptyRecords;
	}

	@Override
	public void process(final Reader reader) {
		assert !isClosed();

		try {
			boolean nothingRead = true;
			int size;
			while ((size = reader.read(buffer)) != -1) {
				nothingRead = false;
				int offset = 0;
				for (int i = 0; i < size; ++i) {
					if (buffer[i] == separator) {
						builder.append(buffer, offset, i - offset);
						offset = i + 1;
						emitRecord();
					}
				}
				builder.append(buffer, offset, size - offset);
			}
			if (!nothingRead) {
				emitRecord();
			}

		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}

	private void emitRecord() {
		final String record = builder.toString();
		if (!skipEmptyRecords || !record.isEmpty()) {
			getReceiver().process(record);
			builder.delete(0, builder.length());
		}
	}

}
