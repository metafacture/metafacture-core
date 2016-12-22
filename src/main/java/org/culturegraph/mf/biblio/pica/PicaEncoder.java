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
package org.culturegraph.mf.biblio.pica;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.MissingIdException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;


/**
 * Encodes an event stream in pica+ format.
 *
 * <strong>Special handling of subfield 'S':</strong> the code of
 * "control subfields" (subfield name='S') will be appended to the fieldName.
 * E.g.: 041A $Saxx would be mapped to the fieldName 041Aa, and xx will be
 * ignored. A recovery of such field to original is not implemented. So the
 * encoder cannot identify an S-field.
 * The S-field special processing can be turned on if the decoder is called
 * with the option: (appendcontrolsubfield="true")
 * The default value of this option is set to "false".
 *
 * @see PicaDecoder
 *
 * @author Yining Li
 *
 */
@Description("Encodes a stream in pica+ format")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-pica")
public final class PicaEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	private static final String FIELD_DELIMITER = "\u001e";
	private static final String SUB_DELIMITER = "\u001f";
	private static final String FIELD_IDN_INTERN = "003@";
	private static final String FIELD_NAME_PATTERN_STRING = "\\d{3}.(/..)?";
	private static final Pattern FIELD_NAME_PATTERN = Pattern.compile(FIELD_NAME_PATTERN_STRING);

	private static StringBuilder builder = new StringBuilder(); //Result of the encoding process

	private boolean entityOpen;         //Flag to inform whether an entity is opened.
	private boolean idnControlSubField; //Flag to inform whether it is the 003@ field.
	private boolean ignoreRecordId;     //Flag to decide whether the record Id is checked.

	private String id;

	@Override
	public void startRecord(final String recordId) {
		// the name is a idn, which should be found in the encoded data under 003@.
		//any rest of the previous record is cleared before the new begins.
		builder.setLength(0);
		this.id = recordId;
		//Now an entity can be opened. But no literal is allowed.
		this.entityOpen = false;
	}

	public void setIgnoreRecordId(final boolean ignoreRecordId) {
		this.ignoreRecordId = ignoreRecordId;
	}

	public boolean getIgnoreRecordId() {
		return this.ignoreRecordId;
	}

	@Override
	public void startEntity(final String name) {
		// Here begins a field (i.e. "028A ", which is given in the name.
		// It is unknown, whether there are any subfields in the field.
		final Matcher fieldNameMatcher = FIELD_NAME_PATTERN.matcher(name);
		if (!fieldNameMatcher.matches()) {
			throw new FormatException(name);
		}
		if (entityOpen) { //No nested entities are allowed in pica+.
			throw new FormatException(name);
		}
		builder.append(name.trim()+ " ");

		idnControlSubField = !ignoreRecordId && FIELD_IDN_INTERN.equals(name.trim());
		//Now literals can be opened but no more entities.
		this.entityOpen = true;
	}

	@Override
	public void literal(final String name, final String value) {
		//A Subfield has one character or digit exactly.
		if (name.length() != 1) {
			throw new FormatException(name);
		}
		if (!entityOpen) {
			throw new FormatException(name); //new exceptions definition for literal out of entity
		}
		final String valueNew = Normalizer.normalize(value, Form.NFD);
		if (idnControlSubField) {
			// it is a 003@ field, the same record id delivered with record should follow
			if (!this.id.equals(value)) {
				throw new MissingIdException(value);
			}
			idnControlSubField = false; //only one record Id will be checked.
		}
		builder.append(SUB_DELIMITER);
		builder.append(name);
		builder.append(valueNew);
}

	@Override
	public void endEntity() {
		builder.append(FIELD_DELIMITER);
		//Now an entity can be opened. But no literal is allowed.
		this.entityOpen = false;
	}

	@Override
	public void endRecord() {
		getReceiver().process(builder.toString());
		//No literal is allowed.
		this.entityOpen = false;
	}

	@Override
	protected void onResetStream() {
		builder.setLength(0);
	}

}
