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
package org.culturegraph.mf.biblio;

import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.MissingIdException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;


/**
 * Parses a raw Mab2 stream (utf-8 encoding assumed). Events are handled by a
 * {@link StreamReceiver}.
 *
 * @see StreamReceiver
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
@Description("Parses a raw Mab2 stream (UTF-8 encoding expected).")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-mab")
public final class MabDecoder
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final String FIELD_END = "\u001e";
	private static final Pattern FIELD_PATTERN =
			Pattern.compile(FIELD_END, Pattern.LITERAL);
	private static final Pattern SUBFIELD_PATTERN =
			Pattern.compile("\u001f", Pattern.LITERAL);
	private static final String RECORD_END = "\u001d";

	private static final int FIELD_NAME_SIZE = 4;
	private static final int HEADER_SIZE = 24;
	private static final String LEADER = "Leader";
	private static final String TYPE = "type";
	private static final String INVALID_FORMAT = "Invalid MAB format";
	private static final String ID_TAG = "001 ";
	private static final int TAG_LENGTH = 4;

	@Override
	public void process(final String record) {
		assert !isClosed();

		if (record.trim().isEmpty()) {
			return;
		}

		getReceiver().startRecord(extractIdFromRecord(record));

		try {
			getReceiver().literal(LEADER, record.substring(0, HEADER_SIZE));
			getReceiver().literal(TYPE, String.valueOf(record.charAt(HEADER_SIZE-1)));
			final String content = record.substring(HEADER_SIZE);
			for (final String part : FIELD_PATTERN.split(content)) {
				if (!part.startsWith(RECORD_END)) {
					final String fieldName = part.substring(0, FIELD_NAME_SIZE).trim();
					final String fieldContent = part.substring(FIELD_NAME_SIZE);
					final String[] subFields = SUBFIELD_PATTERN.split(fieldContent);

					if (subFields.length == 1) {
						getReceiver().literal(fieldName, subFields[0]);
					} else {
						getReceiver().startEntity(fieldName);

						for (int i = 1; i < subFields.length; ++i) {
							final String name = subFields[i].substring(0, 1);
							final String value = subFields[i].substring(1);
							getReceiver().literal(name, value);
						}
						getReceiver().endEntity();
					}
				}
			}
		} catch (final IndexOutOfBoundsException e) {
			throw new FormatException("[" + record + "]", e);
		}

		getReceiver().endRecord();
	}

	private String extractIdFromRecord(final String record) {
		try{
			final int fieldEnd = record.indexOf(FIELD_END, HEADER_SIZE);
			if(record.substring(HEADER_SIZE, HEADER_SIZE + TAG_LENGTH).equals(ID_TAG)){
				return record.substring(HEADER_SIZE + TAG_LENGTH, fieldEnd);
			}
			throw new MissingIdException(record);
		} catch (IndexOutOfBoundsException e) {
			throw new FormatException(INVALID_FORMAT + record, e);
		}
	}

}
