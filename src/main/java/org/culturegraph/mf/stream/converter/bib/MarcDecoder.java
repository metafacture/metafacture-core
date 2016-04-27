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
package org.culturegraph.mf.stream.converter.bib;

import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.converter.IllegalEncodingException;


/**
 * Parses a raw Marc stream. UTF-8 encoding expected. Events are handled by a
 * {@link StreamReceiver}.
 *
 * @see StreamReceiver
 *
 * @author Markus Michael Geipel, Christoph Böhme
 */
@Description("Parses a raw Marc string (UTF-8 encoding expected).")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-marc21")
public final class MarcDecoder
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final String FIELD_DELIMITER = "\u001e";
	private static final String SUB_DELIMITER = "\u001f";
	private static final Pattern FIELD_PATTERN = Pattern.compile(FIELD_DELIMITER, Pattern.LITERAL);
	private static final Pattern SUBFIELD_PATTERN = Pattern.compile(SUB_DELIMITER, Pattern.LITERAL);
	private static final int POS_ENCODING = 9;
	private static final int POS_DIRECTORY = 24;
	private static final int DIRECTORY_ENTRY_WIDTH = 12;
	private static final int TAG_LENGTH = 3;
	private static final int DATA_START_BEGIN = 12;
	private static final int DATA_START_END = 17;

	private static final Object ID_TAG = "001";
	private static final String LEADER = "leader";
	private static final int LEADER_END = 24;

	private boolean checkUtf8Encoding;

	@Override
	public void process(final String record) {
		assert !isClosed();
		process(record, getReceiver(), checkUtf8Encoding);
	}

	public void setCheckUtf8Encoding(final boolean checkUtf8Encoding) {
		this.checkUtf8Encoding = checkUtf8Encoding;
	}

	public static String extractIdFromRecord(final String record) {
		try {
			if (record.substring(POS_DIRECTORY, POS_DIRECTORY + TAG_LENGTH).equals(ID_TAG)) {
				final int start = record.indexOf(FIELD_DELIMITER) + 1;
				final int end = record.indexOf(FIELD_DELIMITER, start);
				return record.substring(start, end);
			}
			throw new MissingIdException(record);

		} catch (IndexOutOfBoundsException exception) {
			throw new FormatException(record, exception);
		}
	}

	public static void process(final String record, final StreamReceiver receiver) {
		process(record, receiver, false);
	}

	public static void process(final String record, final StreamReceiver receiver, final boolean checkUtf8encoding) {
		if (record.trim().isEmpty()) {
			return;
		}

		try {
			if (checkUtf8encoding && record.charAt(POS_ENCODING) != 'a') {
				throw new IllegalEncodingException("UTF-8 encoding expected");
			}
			receiver.startRecord(extractIdFromRecord(record));
			receiver.literal(LEADER, record.substring(0, LEADER_END));

			final int dataStart = Integer.parseInt(record.substring(DATA_START_BEGIN, DATA_START_END));
			final String directory = record.substring(POS_DIRECTORY, dataStart);
			final int numDirEntries = directory.length() / DIRECTORY_ENTRY_WIDTH;
			final String[] fields = FIELD_PATTERN.split(record);

			for (int i = 0; i < numDirEntries; i += 1) {
				final int base = i * 12;
				final String[] subFields = SUBFIELD_PATTERN.split(fields[i + 1]);
				final String tag = directory.substring(base, base + TAG_LENGTH);
				if (tag.charAt(1) == '0' && tag.charAt(0) == '0') {
					receiver.literal(tag, subFields[0]);
				} else {
					receiver.startEntity(tag + subFields[0]);
					for (int j = 1; j < subFields.length; ++j) {
						final String subField = subFields[j];
						receiver.literal(String.valueOf(subField.charAt(0)), subField.substring(1));
					}
					receiver.endEntity();
				}
			}
			receiver.endRecord();
		} catch (IndexOutOfBoundsException exception) {
			throw new FormatException(record, exception);
		}
	}

}
