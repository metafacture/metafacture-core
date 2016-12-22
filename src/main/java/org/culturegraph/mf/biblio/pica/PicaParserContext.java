/*
 * Copyright 2016 Christoph Böhme
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

import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Parser context for the PICA+ parser.The context implements
 * support for normalising the UTF8 encoding of values into NFC
 * on the fly and for skipping fields without any subfields.
 *
 * @author Christoph Böhme
 *
 */
final class PicaParserContext {

	private final StringBuilder builder = new StringBuilder();

	private boolean normalizeUTF8;
	private boolean skipEmptyFields = true;
	private boolean trimFieldNames = true;

	private StreamReceiver receiver;

	private String entityName;
	private boolean literalsEmitted;

	private String subfieldName;

	public void setNormalizeUTF8(final boolean normalizeUTF8) {
		this.normalizeUTF8 = normalizeUTF8;
	}

	public boolean getNormalizeUTF8() {
		return normalizeUTF8;
	}

	public void setSkipEmptyFields(final boolean skipEmptyFields) {
		this.skipEmptyFields = skipEmptyFields;
	}

	public boolean getSkipEmptyFields() {
		return skipEmptyFields;
	}

	public void setTrimFieldNames(final boolean trimFieldNames) {
		this.trimFieldNames = trimFieldNames;
	}

	public boolean getTrimFieldNames() {
		return trimFieldNames;
	}

	public void setReceiver(final StreamReceiver receiver) {
		this.receiver = receiver;
	}

	public void reset() {
		getTextAndReset();
		entityName = null;
		literalsEmitted = false;
		subfieldName = null;
	}

	protected void appendText(final char ch) {
		builder.append(ch);
	}

	protected void emitStartEntity() {
		// Output of the startEntity event is postponed
		// until a literal is emitted in order to able
		// to skip empty entities

		entityName = getTextAndReset();
		if (trimFieldNames) {
			entityName = entityName.trim();
		}
		literalsEmitted = false;
	}

	protected void emitEndEntity() {
		if (!literalsEmitted) {
			if (skipEmptyFields || entityName.isEmpty()) {
				return;
			}
			receiver.startEntity(entityName);
			entityName = null;
		}
		receiver.endEntity();
	}

	protected void setSubfieldName(final char name) {
		subfieldName = String.valueOf(name);
	}

	protected void emitLiteral() {
		assert subfieldName != null;
		assert entityName != null || literalsEmitted;

		if (entityName != null) {
			receiver.startEntity(entityName);
			entityName = null;
			literalsEmitted = true;
		}

		String value = getTextAndReset();
		if (normalizeUTF8) {
			value = Normalizer.normalize(value, Form.NFC);
		}
		receiver.literal(subfieldName, value);
		subfieldName = null;
	}

	private String getTextAndReset() {
		final String text = builder.toString();
		builder.setLength(0);
		return text;
	}

}
