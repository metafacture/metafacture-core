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
package org.culturegraph.mf.formeta;

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
 * Reads a stream of formeta data and splits between each top-level element.
 *
 * @author Christoph Böhme
 *
 */
@In(Reader.class)
@Out(String.class)
@Description("Reads a stream of formeta data and splits between each top-level element")
@FluxCommand("as-formeta-records")
public final class FormetaRecordsReader extends
		DefaultObjectPipe<Reader, ObjectReceiver<String>> {

	private static final int BUFFER_SIZE = 1024 * 1024 * 16;

	private final StringBuilder builder = new StringBuilder();
	private final char[] buffer = new char[BUFFER_SIZE];

	@Override
	public void process(final Reader reader) {
		assert !isClosed();

		try {
			boolean readSomething = false;
			boolean inQuotedText = false;
			int groupLevel = 0;
			int size;
			while ((size = reader.read(buffer)) != -1) {
				readSomething = true;
				int offset = 0;
				for (int i = 0; i < size; ++i) {
					switch (buffer[i]) {
					case Formeta.ESCAPE_CHAR:
						i += 1; // Skip next character
						break;
					case Formeta.GROUP_START:
						if (!inQuotedText) {
							groupLevel += 1;
						}
						break;
					case Formeta.GROUP_END:
						if (!inQuotedText) {
							groupLevel -= 1;
						}
						// Fall through
					case Formeta.ITEM_SEPARATOR:
						if (!inQuotedText && groupLevel == 0) {
							builder.append(buffer, offset, i - offset + 1);
							offset = i + 1;
							emitRecord();
						}
						break;
					case Formeta.QUOT_CHAR:
						inQuotedText = !inQuotedText;
						break;
					}
				}
				builder.append(buffer, offset, size - offset);
			}
			if (readSomething) {
				emitRecord();
			}

		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}

	private void emitRecord() {
		final String record = builder.toString();
		getReceiver().process(record);
		builder.delete(0, builder.length());
	}

}
