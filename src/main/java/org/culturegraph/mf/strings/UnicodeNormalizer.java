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
package org.culturegraph.mf.strings;

import java.text.Normalizer;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Normalises Unicode characters in strings. Unicode normalisation converts
 * between precomposed characters and composed characters. There are four
 * different forms of conversion which can be selected using
 * {@link #setNormalizationForm(Normalizer.Form)}.
 *
 * @author Christoph Böhme
 *
 */
@Description("Normalizes diacritics in Unicode strings.")
@In(String.class)
@Out(String.class)
@FluxCommand("normalize-unicode-string")
public final class UnicodeNormalizer extends
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	/**
	 * The default value for {@link #setNormalizationForm(Normalizer.Form)}.
	 */
	public static final Normalizer.Form DEFAULT_NORMALIZATION_FORM =
			Normalizer.Form.NFC;

	private Normalizer.Form normalizationForm = DEFAULT_NORMALIZATION_FORM;

	/**
	 * Sets the normalisation form used for normalising strings.
	 * <p>
	 * The default value is NFC.
	 * <p>
	 * This parameter may be set at any time during processing. It becomes
	 * effective with the next string that is received.
	 *
	 * @param normalizationForm the normalisation form to use.
	 *
	 */
	public void setNormalizationForm(final Normalizer.Form normalizationForm) {
		this.normalizationForm = normalizationForm;
	}

	public Normalizer.Form getNormalizationForm() {
		return normalizationForm;
	}

	@Override
	public void process(final String str) {
		assert null != str;
		assert !isClosed();
		getReceiver().process(Normalizer.normalize(str, normalizationForm));
	}

}
