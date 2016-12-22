/*
 * Copyright 2014 Deutsche Nationalbibliothek
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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.culturegraph.mf.flowcontrol.StreamBuffer;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Groups multiscript fields in entities.
 * <p>
 * In Pica records some fields can be repeated if they contain contents with a
 * non-latin script. These fields can be recognised by the existence of the
 * subfields {@code $U} and {@code $T}. {@code $U} contains the name of the
 * script used for the values of the fields. {@code $T} contains a number which
 * is used to group fields together which contain the same contents in different
 * scripts. All fields having the same field name and the same number in
 * {@code $T} are considered to belong together.
 * <p>
 * This module scans the input stream for Pica multiscript fields and remodels
 * them by merging all fields belong to the same multiscript group into one
 * entity. This entity has the original field name. Within this entity a new
 * entity for each of the original fields is created. These entities are named
 * depending on the type of script used. Three scripts are distinguished:
 * <ul>
 *   <li>Latin
 *   <li>NonLatinLR
 *   <li>NonLatinRL
 * </ul>
 *
 * The following example shows how the input
 *
 * <pre>
 *    021A $T 01 $U Latn $a Title
 *    021A $T 01 $U Grek $a Greek title
 *    021C $T 01 $U Latn $a Subseries A
 *    021C $T 02 $U Latn $a Subseries B
 *    021C $T 01 $U Grek $a Greek subseries A
 *    021C $T 02 $U Grek $a Greek subseries B
 * </pre>
 *
 * is remodeled into
 *
 * <pre>
 *    021A {
 *        Latin { T: 01, U: Latn, a: Title }
 *        NonLatinLR { T: 01, U: Grek, a: Greek title }
 *    }
 *    021C {
 *        Latin { T: 01, U: Latn, a: Subseries A }
 *        NonLatinLR { T: 01, U: Grek, a: Greek subseries A }
 *    }
 *    021C {
 *        Latin { T: 02, U: Latn, a: Subseries B }
 *        NonLatinLR { T: 02, U: Grek, a: Greek subseries B}
 *    }
 * </pre>
 *
 * Fields which do not contain subfields $U and $T are passed through the module
 * unaffected. If a multiscript field is encountered which only exists in a
 * single script it is not remodeled but simply passed through. The module
 * assumes that no more than two script-variants of a field exist. If a field
 * with more than two variants is encountered then the behaviour of
 * {@code PicaMultiscriptRemodeler} is undefined.
 * <p>
 * The order of the output is determined by the order of the second occurrences
 * of the multiscript fields. Multiscript fields without a second occurrences are
 * output when the second occurrence of a field with a greater group number is
 * encountered.
 * <p>
 * If a field contains only $U or $T but not both, the field is simply passed
 * through.
 * <p>
 * If the sequence of input events does not follow the Pica record definitions
 * (order of fields, nesting of entities) the behaviour of this module is
 * undefined.
 *
 * @author Christoph Böhme
 *
 */
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@Description("Groups multiscript fields in entities")
@FluxCommand("remodel-pica-multiscript")
public final class PicaMultiscriptRemodeler extends
		DefaultStreamPipe<StreamReceiver> {

	public static final String ENTITY_NAME_FOR_LATIN = "Latin";
	public static final String ENTITY_NAME_FOR_NON_LATIN_LR = "NonLatinLR";
	public static final String ENTITY_NAME_FOR_NON_LATIN_RL = "NonLatinRL";

	private static final BufferedField BEFORE_FIRST_FIELD = new BufferedField("", null);

	private static final String GROUP_SUBFIELD = "T";
	private static final String SCRIPT_SUBFIELD = "U";

	private static final String LATIN_SCRIPT = "Latn";
	private static final String ARABIC_SCRIPT = "Arab";
	private static final String HEBREW_SCRIPT = "Hebr";

	private BufferedField currentField;
	private BufferedField lastField;

	private final SortedMap<String, BufferedField> bufferedFields = new TreeMap<String, BufferedField>();

	@Override
	public void startRecord(final String identifier) {
		getReceiver().startRecord(identifier);

		currentField = null;
		lastField = BEFORE_FIRST_FIELD;

		bufferedFields.clear();
	}

	@Override
	public void endRecord() {
		emitAsSingleMultiscriptFields(bufferedFields);
		getReceiver().endRecord();
	}

	@Override
	public void startEntity(final String name) {
		currentField = new BufferedField(name);
		currentField.stream.setReceiver(getReceiver());

		if (!lastField.name.equals(currentField.name)) {
			emitAsSingleMultiscriptFields(bufferedFields);
		}
	}

	@Override
	public void endEntity() {
		if (currentField.group == null || currentField.script == null) {
			emitNonMultiscriptField();
		} else {
			if (bufferedFields.containsKey(currentField.group)) {
				emitAsSingleMultiscriptFields(getSingleMultiscriptFieldsBeforeCurrentField());
				emitRemodeledMultiscriptField(bufferedFields.remove(currentField.group), currentField);
			} else {
				bufferMultiscriptField(currentField);
			}
		}

		lastField = currentField;
		currentField = null;
	}

	@Override
	public void literal(final String name, final String value) {
		currentField.stream.literal(name, value);

		if (GROUP_SUBFIELD.equals(name)) {
			currentField.group = value;
		} else if (SCRIPT_SUBFIELD.equals(name)) {
			currentField.script = value;
		}
	}

	private void bufferMultiscriptField(final BufferedField field) {
		bufferedFields.put(field.group, field);
	}

	private Map<?, BufferedField> getSingleMultiscriptFieldsBeforeCurrentField() {
		return bufferedFields.headMap(currentField.group);
	}

	private void emitNonMultiscriptField() {
		getReceiver().startEntity(currentField.name);
		currentField.stream.replay();
		getReceiver().endEntity();
	}

	private void emitRemodeledMultiscriptField(final BufferedField firstField, final BufferedField secondField) {
		getReceiver().startEntity(firstField.name);

		getReceiver().startEntity(mapScriptToEntityName(firstField.script));
		firstField.stream.replay();
		getReceiver().endEntity();

		getReceiver().startEntity(mapScriptToEntityName(secondField.script));
		secondField.stream.replay();
		getReceiver().endEntity();

		getReceiver().endEntity();
	}

	private void emitAsSingleMultiscriptFields(final Map<?, BufferedField> fields) {
		for (final BufferedField field : fields.values()) {
			getReceiver().startEntity(field.name);
			field.stream.replay();
			getReceiver().endEntity();
		}
		fields.clear();
	}

	private String mapScriptToEntityName(final String script) {
		if (LATIN_SCRIPT.equals(script)) {
			return ENTITY_NAME_FOR_LATIN;
		} else if (ARABIC_SCRIPT.equals(script)
				|| HEBREW_SCRIPT.equals(script)) {
			return ENTITY_NAME_FOR_NON_LATIN_RL;
		}
		return ENTITY_NAME_FOR_NON_LATIN_LR;
	}

	private static class BufferedField {

		public String group;
		public String script;

		public final String name;
		public final StreamBuffer stream;

		public BufferedField(final String name) {
			this(name, new StreamBuffer());
		}

		public BufferedField(final String name, final StreamBuffer stream) {
			this.group = null;
			this.script = null;
			this.name = name;
			this.stream = stream;
		}

	}

}
