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
package org.culturegraph.mf.stream.converter;

import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.CGEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Reads Strings CGEntity format.
 * 
 * @see CGEntityEncoder
 * 
 * @author Markus Michael Geipel, Christoph Böhme
 * 
 */
@Description("Reads Strings CGEntity format.")
@In(String.class)
@Out(StreamReceiver.class)
public final class CGEntityDecoder 
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final Pattern FIELD_PATTERN = Pattern.compile(
			String.valueOf(CGEntity.FIELD_DELIMITER), Pattern.LITERAL);
	private static final Pattern SUBFIELD_PATTERN = Pattern.compile(
			String.valueOf(CGEntity.SUB_DELIMITER), Pattern.LITERAL);

	private static final Logger LOG = LoggerFactory
			.getLogger(CGEntityDecoder.class);

	@Override
	public void process(final String record) {
		process(record, getReceiver());
	}

	public static void process(final String record, final StreamReceiver receiver) {
		try {
			final String[] fields = FIELD_PATTERN.split(record);
			receiver.startRecord(fields[0]);
			for (int i = 1; i < fields.length; ++i) {
				final char firstChar = fields[i].charAt(0);
				if (firstChar == CGEntity.LITERAL_MARKER) {
					final String[] parts = SUBFIELD_PATTERN
							.split(fields[i], -1);
					receiver.literal(
							parts[0].substring(1),
							parts[1].replace(CGEntity.NEWLINE_ESC,
									CGEntity.NEWLINE));
				} else if (firstChar == CGEntity.ENTITY_START_MARKER) {
					receiver.startEntity(fields[i].substring(1));
				} else if (firstChar == CGEntity.ENTITY_END_MARKER) {
					receiver.endEntity();
				} else if (firstChar == CGEntity.NEWLINE) {
					LOG.debug("unexpected newline");
				} else {
					throw new FormatException(record);
				}
			}
			receiver.endRecord();
		} catch (IndexOutOfBoundsException exception) {
			throw new FormatException(record, exception);
		}		
	}
	
}