/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.iso2709;

/**
 * Builds a list of fields in ISO 2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
final class FieldsBuilder {

	private final StringBuilder fields = new StringBuilder();

	private final int identifierLength;

	private boolean inField;
	private int undoMarker = -1;

	public FieldsBuilder(final RecordFormat format) {
		identifierLength = format.getIdentifierLength();
	}

	public int startField(final String indicators) {
		requireNotInField();
		inField = true;
		undoMarker = fields.length();
		fields.append(indicators);
		return undoMarker;
	}

	public int endField() {
		requireInField();
		inField = false;
		fields.append(Iso2709Format.FIELD_SEPARATOR);
		return fields.length();
	}

	public void undoLastField() {
		assert undoMarker > -1;
		fields.setLength(undoMarker);
		undoMarker = -1;
	}

	public void appendValue(final String value) {
		requireInField();
		fields.append(value);
	}

	public void appendSubfield(final String identifier, final String value) {
		requireInField();
		if (identifierLength > 0) {
			fields.append(Iso2709Format.IDENTIFIER_MARKER);
			fields.append(identifier);
		}
		fields.append(value);
	}

	public void reset() {
		fields.setLength(0);
		undoMarker = -1;
		inField = false;
	}

	public int length() {
		return fields.length() + 1;
	}

	@Override
	public String toString() {
		requireNotInField();
		return fields.toString() + Iso2709Format.RECORD_SEPARATOR;
	}

	private void requireInField() {
		if (!inField) {
			throw new IllegalStateException("need to be in field");
		}
	}

	private void requireNotInField() {
		if (inField) {
			throw new IllegalStateException("must not be in field");
		}
	}

}
