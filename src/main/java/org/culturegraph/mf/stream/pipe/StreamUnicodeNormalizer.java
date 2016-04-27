/*
 * Copyright 2015 Christoph Böhme
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

package org.culturegraph.mf.stream.pipe;

import java.text.Normalizer;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Normalises Unicode characters in record identifiers, entity and literal names
 * and literal values. Unicode normalisation converts between precomposed
 * characters and composed characters. There are four different forms of
 * conversion which can be selected using {@code setNormalizationForm}. By
 * default {@code StreamUnicodeNormalizer} converts from composed characters to
 * precomposed characters using the {@link java.text.Normalizer.Form#NFC}
 * conversion form. In the default configuration only literal values are
 * converted. The various {@code setNormalize*} methods can be used to change
 * this behaviour.
 *
 * @author Christoph Böhme
 */
@Description("Normalises composed and decomposed Unicode characters.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("normalize-unicode-stream")
public final class StreamUnicodeNormalizer
		extends DefaultStreamPipe<StreamReceiver> {

	private boolean normalizeIds;
	private boolean normalizeKeys;
	private boolean normalizeValues = true;

	private Normalizer.Form normalizationForm = Normalizer.Form.NFC;

	/**
	 * Set to {@code true} to normalise record identifiers. The default value is
	 * {@code false}.
	 */
	public void setNormalizeIds(final boolean normalizeIds) {
		this.normalizeIds = normalizeIds;
	}

	public boolean getNormalizeIds() {
		return normalizeIds;
	}

	/**
	 * Set to {@code true} to normalise names of entities and literals. The
	 * default value is {@code false}.
	 */
	public void setNormalizeKeys(final boolean normalizeKeys) {
		this.normalizeKeys = normalizeKeys;
	}

	public boolean getNormalizeKeys() {
		return normalizeKeys;
	}

	/**
	 * Set to {@code true} to normalise literal values. The default value is
	 * {@code true}.
	 */
	public void setNormalizeValues(final boolean normalizeValues) {
		this.normalizeValues = normalizeValues;
	}

	public boolean getNormalizeValues() {
		return normalizeValues;
	}

	/**
	 * Sets the normalisation form used for normalising record identifiers,
	 * entity and literal names and values. The default value is {@code NFC}.
	 */
	public void setNormalizationType(
			final Normalizer.Form normalizationForm) {
		this.normalizationForm = normalizationForm;
	}

	public Normalizer.Form getNormalizationType() {
		return normalizationForm;
	}

	@Override
	public void startRecord(final String identifier) {
		final String normalizedIdentifier =
				normalizeIds ? normalize(identifier) : identifier;

		getReceiver().startRecord(normalizedIdentifier);
	}

	@Override
	public void endRecord() {
		getReceiver().endRecord();
	}

	@Override
	public void startEntity(final String name) {
		final String normalizedName =
				normalizeKeys ? normalize(name) : name;

		getReceiver().startEntity(normalizedName);
	}

	@Override
	public void endEntity() {
		getReceiver().endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		final String normalizedName =
				normalizeKeys ? normalize(name) : name;
		final String normalizedValue=
				normalizeValues ? normalize(value) : value;

		getReceiver().literal(normalizedName, normalizedValue);
	}

	private String normalize(final String string) {
		return Normalizer.normalize(string, normalizationForm);
	}

}
