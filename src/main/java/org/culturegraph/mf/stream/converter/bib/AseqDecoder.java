/*
 *  Copyright 2015 Lars G. Svensson
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.converter.bib;

import java.util.regex.Pattern;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Parses a raw Aseq stream. UTF-8 encoding expected. Events are handled by a
 * {@link StreamReceiver}.
 *
 * @see StreamReceiver
 *
 * @author Lars G. Svensson
 */
@Description("Parses a raw Aseq record (UTF-8 encoding expected).")
@In(String.class)
@Out(StreamReceiver.class)
public final class AseqDecoder extends
		DefaultObjectPipe<String, StreamReceiver> {

	private static final String FIELD_DELIMITER = "\n";
	private static final String SUB_DELIMITER = "\u001f";
	private static final Pattern FIELD_PATTERN = Pattern.compile(
			FIELD_DELIMITER, Pattern.LITERAL);
	private static final Pattern SUBFIELD_PATTERN = Pattern.compile(
			SUB_DELIMITER, Pattern.LITERAL);
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
		process(record, getReceiver(), this.checkUtf8Encoding);
	}

	public void setCheckUtf8Encoding(final boolean checkUtf8Encoding) {
		this.checkUtf8Encoding = checkUtf8Encoding;
	}

	public static void process(final String record,
			final StreamReceiver receiver) {
		process(record, receiver, false);
	}

	public static void process(String record, final StreamReceiver receiver,
			final boolean checkUtf8encoding) {
		record = record.trim();
		if (record.isEmpty()) {
			return;
		}
		final String[] lines = record.split(FIELD_DELIMITER);
		for (int i = 0; i < lines.length; i++) {
			final String field = lines[i];
			if (i == 0) {
				receiver.startRecord(field.substring(0, 9));
			}
			final String category = field.substring(10, 15).trim();
			final String scriptCode = field.substring(16, 17);
			final String fieldContent = field.substring(18).trim();
			if (!fieldContent.startsWith("$$")) {
				receiver.literal(category, fieldContent);
			} else {
				receiver.startEntity(category);
				final String[] subfields = fieldContent.split("\\$\\$");
				for (int j = 0; j < subfields.length; j++) {
					if (!"".equals(subfields[j])) {
						final String subfield = subfields[j];
						final String subfieldCode = subfield.substring(0, 1);
						final String subfieldContent = subfield.substring(1);
						receiver.literal(subfield.substring(0, 1),
								subfield.substring(1));
					}
				}
				receiver.endEntity();
			}
		}
		receiver.endRecord();
	}

}
