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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.text.Normalizer;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamUnicodeNormalizer}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamUnicodeNormalizerTest {

	private static final String RECORD_ID = "id";
	private static final String ENTITY_NAME = "entity-name";
	private static final String LITERAL_NAME = "literal-name";
	private static final String LITERAL_VALUE = "literal-value";

	private static final String VALUE_WITH_DIACRITICS =
			"Bauer, Sigmund: Über den Einfluß der Ackergeräthe auf den Reinertrag.";
	private static final String VALUE_WITH_PRECOMPOSED_CHARS =
			"Bauer, Sigmund: Über den Einfluß der Ackergeräthe auf den Reinertrag.";

	private static final String ID_WITH_DIACRITICS = "id-Üä";
	private static final String ID_WITH_PRECOMPOSED_CHARS = "id-Üä";

	private static final String KEY_WITH_DIACRITICS = "key-Üä";
	private static final String KEY_WITH_PRECOMPOSED_CHARS = "key-Üä";

	private StreamUnicodeNormalizer streamUnicodeNormalizer;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		streamUnicodeNormalizer = new StreamUnicodeNormalizer();
		streamUnicodeNormalizer.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		streamUnicodeNormalizer.closeStream();
	}

	@Test
	public void shouldForwardAllEvents() {
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.startEntity(ENTITY_NAME);
		streamUnicodeNormalizer.literal(LITERAL_NAME, LITERAL_VALUE);
		streamUnicodeNormalizer.endEntity();
		streamUnicodeNormalizer.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_NAME);
		ordered.verify(receiver).literal(LITERAL_NAME, LITERAL_VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldNormalizeValuesToNFCByDefault() {
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.literal(LITERAL_NAME, VALUE_WITH_DIACRITICS);
		streamUnicodeNormalizer.endRecord();

		verify(receiver).literal(LITERAL_NAME, VALUE_WITH_PRECOMPOSED_CHARS);
	}

	@Test
	public void shouldNotNormalizeValuesIfConfigured() {
		streamUnicodeNormalizer.setNormalizeValues(false);
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.literal(LITERAL_NAME, VALUE_WITH_DIACRITICS);
		streamUnicodeNormalizer.endRecord();

		verify(receiver).literal(LITERAL_NAME, VALUE_WITH_DIACRITICS);
	}

	@Test
	public void shouldIgnoreNullValues() {
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.literal(LITERAL_NAME, null);
		streamUnicodeNormalizer.endRecord();

		verify(receiver).literal(LITERAL_NAME, null);
	}

	@Test
	public void shouldNotNormalizeIdByDefault() {
		streamUnicodeNormalizer.startRecord(ID_WITH_DIACRITICS);
		streamUnicodeNormalizer.endRecord();

		verify(receiver).startRecord(ID_WITH_DIACRITICS);
	}

	@Test
	public void shouldNormalizeIdToNFCIfConfigured() {
		streamUnicodeNormalizer.setNormalizeIds(true);
		streamUnicodeNormalizer.startRecord(ID_WITH_DIACRITICS);
		streamUnicodeNormalizer.endRecord();

		verify(receiver).startRecord(ID_WITH_PRECOMPOSED_CHARS);
	}

	@Test
	public void shouldNotNormalizeKeyByDefault() {
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.literal(KEY_WITH_DIACRITICS, LITERAL_VALUE);
		streamUnicodeNormalizer.startEntity(KEY_WITH_DIACRITICS);
		streamUnicodeNormalizer.endEntity();
		streamUnicodeNormalizer.endRecord();

		verify(receiver).literal(KEY_WITH_DIACRITICS, LITERAL_VALUE);
		verify(receiver).startEntity(KEY_WITH_DIACRITICS);
	}

	@Test
	public void shouldNormalizeKeysIfConfigured() {
		streamUnicodeNormalizer.setNormalizeKeys(true);
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.literal(KEY_WITH_DIACRITICS, LITERAL_VALUE);
		streamUnicodeNormalizer.startEntity(KEY_WITH_DIACRITICS);
		streamUnicodeNormalizer.endEntity();
		streamUnicodeNormalizer.endRecord();

		verify(receiver).literal(KEY_WITH_PRECOMPOSED_CHARS, LITERAL_VALUE);
		verify(receiver).startEntity(KEY_WITH_PRECOMPOSED_CHARS);
	}

	@Test
	public void shouldNormalizeToNFDIfConfigured() {
		streamUnicodeNormalizer.setNormalizationForm(Normalizer.Form.NFD);
		streamUnicodeNormalizer.startRecord(RECORD_ID);
		streamUnicodeNormalizer.literal(LITERAL_NAME,
				KEY_WITH_PRECOMPOSED_CHARS);
		streamUnicodeNormalizer.endRecord();

		verify(receiver).literal(LITERAL_NAME, KEY_WITH_DIACRITICS);
	}

}
