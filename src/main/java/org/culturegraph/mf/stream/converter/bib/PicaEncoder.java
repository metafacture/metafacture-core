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
import org.culturegraph.mf.framework.DefaultStreamPipe;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


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
@Description("Encodes a stream in pica+ Format")
@In(StreamReceiver.class)
@Out(String.class)
public final class PicaEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	private static final String FIELD_DELIMITER = "\u001e";
	private static final String SUB_DELIMITER = "\u001f";
	private boolean idnControlSubField;
	private boolean recordOpen;
	private boolean entityOpen;
	private StringBuilder builder = new StringBuilder();
	private String id="";

	private static final String FIELD_NAME_PATTERN_STRING = "\\d{3}.(/..)?";
	private static final Pattern FIELD_NAME_PATTERN = Pattern.compile(FIELD_NAME_PATTERN_STRING);
	private boolean ignoreRecordId;
	
	/**
	 * For each field in the stream the method calls:
	 * <ol>
	 * <li>receiver.startEntity</li>
	 * <li>receiver.literal for each subfield of the field</li>
	 * <li>receiver.endEntity</li>
	 * </ol>
	 * Fields without any subfield will be skipped.<br>
	 * 
	 * @param record
	 */
	@Override
	public void startRecord(final String recordId) {
		// the name is a idn, which should be found in the encoded data under 003@.
		//any rest of the previous record is cleared before the new begins.
		builder.setLength(0); 
		this.id = recordId;
		//Now an entity can be opened. But no literal is allowed.
		this.recordOpen = true;
		this.entityOpen = false;
	}

	public void setIgnoreRecordId(final boolean ignoreRecordId) {
		this.ignoreRecordId = ignoreRecordId;
	}
	
	public boolean getIgnoreRecordId() {
		return this.ignoreRecordId;
	}
	
	protected void compareIdFromRecord(final String recordId) {
		if (this.id.equals(recordId)) {
			idnControlSubField = false; //only test this context.
			return;
		}
		throw new MissingIdException(recordId);
	}
	

	@Override
	public void startEntity(final String name) {
	// Here begins a field (i.e. "028A ", which is given in the name.
	// It is unknown, whether there are any subfields in the field.
		final Matcher fieldNameMatcher = FIELD_NAME_PATTERN.matcher(name);
		if (fieldNameMatcher.find()) {
			builder.append(name.trim()+ " ");
		}
		else {
			throw new FormatException(name);
		}
		if (name.trim().equals("003@") && !getIgnoreRecordId()) {
			//Time to check record Id in the following subfield.
			idnControlSubField = true;
		}else {
			//No check is necessary. 
			idnControlSubField = false;
		}
		//Now literals can be opened. But no entities are allowed.
		if (recordOpen)
			this.entityOpen = true;
	}

	@Override
	public void literal(final String name, final String value) {
		//A Subfield has one character or digit exactly.
		if (name.length()!=1){
			throw new FormatException(name);
		} else if (!entityOpen){
			throw new FormatException(name); //new exceptions define!!!! tODo
		}
		final String valueNew = Normalizer.normalize(value, Form.NFD);
		if (idnControlSubField){
			// it is a 003@ field, the same record id delivered with record should follow
			compareIdFromRecord(value);
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
		builder.setLength(0);
		//Now a record can be opened. But no literal and entity are allowed.
		this.recordOpen = false;
		this.entityOpen = false;
	}
	@Override
	protected void onResetStream() {
		builder.setLength(0);
	}

}
