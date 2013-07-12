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
 * @see PicaEncoder
 * 
 * @author Markus Michael Geipel, Christoph Böhme, Yining Li
 *
 */
@Description("Encodes a stream in pica+ Format")
@In(StreamReceiver.class)
@Out(String.class)
public class PicaEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	private static final String FIELD_DELIMITER = "\u001e";
	private static final String SUB_DELIMITER = "\u001f";
	private static boolean idnControlSubField = false;
	private StringBuilder builder = new StringBuilder();
	private String idn="";

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
	 * fieldName. E.g.: 041A $Saxx would be mapped to the fieldName 041Aa,
	 * and xx will be ignored. A recovery of such field to original is not implemented.
	 * So the encoder cannot identify a S-field. The S-field special processing 
	 * can be turn of if the call of decode with the option:
	 * (appendcontrolsubfield="false")
	 * which default is set to true. 
	 * 
	 * @param record
	 */
	@Override
	public final void startRecord(final String name) {
		// the name is a idn, which should be found in the encoded data under 003@.
		this.idn = name;
	}

	public final boolean compareIdFromRecord(final String gndId) {
		if (this.idn.equals(gndId)) {
			idnControlSubField = false; //only test this context.
			return true;
		}
		throw new MissingIdException(gndId);
	}
	

	@Override
	public final void startEntity(final String name) {
	// Here begins a field (i.e. "028A ", which is given in the name.
	// It is unknown, whether there are any subfields in the field.
		builder.append(name.trim()+ " ");
		if (name.trim().equals("003@")) {
			//Time to check nid
			idnControlSubField = true;
		}else {
			//No check is necessary.
			idnControlSubField = false;
		}
	}

	@Override
	public final void literal(final String name, final String value) {
		//
		final String value_new = Normalizer.normalize(value, Form.NFD);
		if (idnControlSubField == true){
			// it is a 003@ field, the same nid delivered with record should follow
			if (compareIdFromRecord(value)) idnControlSubField = false;
		}
		builder.append(SUB_DELIMITER);
		builder.append(name);
		builder.append(value_new);
	}

	@Override
	public final void endEntity() {
		builder.append(FIELD_DELIMITER);
	}

	@Override
	public final void endRecord() {
		getReceiver().process(builder.toString());
		builder = new StringBuilder();
	}

}
