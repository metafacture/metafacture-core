/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * Parses a raw Picaplus stream (utf8 encoding assumed). Events are handled by a
 * {@link StreamReceiver}.
 * 
 * @see StreamReceiver
 * 
 * @author Markus Michael Geipel, Christoph Böhme
 * 
 */
@Description("Parses a raw Picaplus stream (utf8 encoding assumed).")
@In(String.class)
@Out(StreamReceiver.class)
public final class PicaDecoder 
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final String FIELD_DELIMITER = "\u001e";
	private static final String SUB_DELIMITER = "\u001f";
	private static final Pattern FIELD_PATTERN = Pattern.compile(
			FIELD_DELIMITER, Pattern.LITERAL);
	private static final Pattern SUBFIELD_PATTERN = Pattern.compile(
			SUB_DELIMITER, Pattern.LITERAL);
	private static final String ID_PATTERN_STRING = FIELD_DELIMITER + "003@ "
			+ SUB_DELIMITER + "0(.*?)" + FIELD_DELIMITER;
	private static final Pattern ID_PATTERN = Pattern
			.compile(ID_PATTERN_STRING);

	/**
	 * For each field in the stream the method calls:
	 * <ol>
	 * <li>receiver.startEntity</li>
	 * <li>receiver.literal for each subfield of the field</li>
	 * <li>receiver.endEntity</li>
	 * </ol>
	 * Fields without any subfield will be skipped.<br>
	 * <strong>Special handling of subfield 'S':</strong> the code of
	 * "control subfields" (subfield name='S') will be appended to the
	 * fieldName. E.g.: 041A $Sa would be mapped to the fieldName 041Aa
	 * 
	 * @param record
	 */
	@Override
	public void process(final String record) {
		assert !isClosed();
		process(record, getReceiver());
	}

	public static String extractIdFromRecord(final String record) {
		final Matcher idMatcher = ID_PATTERN.matcher(record);
		if (idMatcher.find()) {
			return idMatcher.group(1);
		}
		throw new MissingIdException(record);
	}
	
	public static void process(final String rawRecord, final StreamReceiver receiver) {
		if (rawRecord.trim().isEmpty()) {
			return;
		}
		
		final String record = Normalizer.normalize(rawRecord, Form.NFC);
		try {
			receiver.startRecord(extractIdFromRecord(record));
			
			for (String field : FIELD_PATTERN.split(record)) {
				final String[] subfields = SUBFIELD_PATTERN.split(field);
				if (subfields.length > 1) {
					final String fieldName;
					final int firstSubfield;
					if (subfields[1].charAt(0) == 'S') {
						fieldName = subfields[0].trim() + subfields[1].charAt(1);
						firstSubfield = 2;
					} else {
						fieldName = subfields[0].trim();
						firstSubfield = 1;
					}
	
					receiver.startEntity(fieldName);
	
					for (int i = firstSubfield; i < subfields.length; ++i) {
						final String subfield = subfields[i];
						receiver.literal(subfield.substring(0, 1),
								subfield.substring(1));
					}
					receiver.endEntity();
				}
			}
			
			receiver.endRecord();
		} catch (IndexOutOfBoundsException e) {
			throw new FormatException(e);
		} 
	}
	
}
