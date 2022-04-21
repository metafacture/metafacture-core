/*
 * Copyright 2021 hbz NRW
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

package org.metafacture.metafix;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class HashValueTest {

    private static final String FIELD = "field";
    private static final String OTHER_FIELD = "other field";
    private static final String ALTERNATE_FIELD = FIELD.replace("e", "i");

    private static final String FIELD_CHARACTER_CLASS = FIELD.replace("e", "[aeiou]");
    private static final String FIELD_ALTERNATION = FIELD + "|" + ALTERNATE_FIELD;
    private static final String FIELD_WILDCARD = FIELD.replace("e", "?");

    private static final Value VALUE = new Value("value");
    private static final Value OTHER_VALUE = new Value("other value");

    public HashValueTest() {
    }

    @Test
    public void shouldSatisfyEqualsContract() {
        EqualsVerifier.forClass(Value.Hash.class)
            .withPrefabValues(Value.class, Value.newArray(), Value.newHash())
            .verify();
    }

    @Test
    public void shouldNotContainMissingField() {
        final Value.Hash hash = newHash();
        Assertions.assertFalse(hash.containsField(FIELD));
    }

    @Test
    public void shouldContainExistingField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertTrue(hash.containsField(FIELD));
    }

    @Test
    public void shouldNotContainNullValue() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, null);

        Assertions.assertFalse(hash.containsField(FIELD));
    }

    @Test
    public void shouldContainCharacterClassField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertTrue(hash.containsField(FIELD_CHARACTER_CLASS));
    }

    @Test
    public void shouldContainAlternationField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertTrue(hash.containsField(FIELD_ALTERNATION));
    }

    @Test
    public void shouldContainWildcardField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertTrue(hash.containsField(FIELD_WILDCARD));
    }

    @Test
    public void shouldBeEmptyByDefault() {
        final Value.Hash hash = newHash();
        Assertions.assertTrue(hash.isEmpty());
    }

    @Test
    public void shouldNotBeEmptyAfterAddingValue() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertFalse(hash.isEmpty());
    }

    @Test
    public void shouldNotAddNullValue() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, null);

        Assertions.assertTrue(hash.isEmpty());
    }

    @Test
    public void shouldGetSizeOfDefaultMapping() {
        final Value.Hash hash = newHash();
        Assertions.assertEquals(0, hash.size());
    }

    @Test
    public void shouldGetSizeAfterAddingValues() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);

        Assertions.assertEquals(2, hash.size());
    }

    @Test
    public void shouldNotGetMissingField() {
        final Value.Hash hash = newHash();
        Assertions.assertNull(hash.get(FIELD));
    }

    @Test
    public void shouldGetExistingField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertEquals(VALUE, hash.get(FIELD));
    }

    @Test
    public void shouldGetSingleCharacterClassField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertEquals(VALUE, hash.get(FIELD_CHARACTER_CLASS));
    }

    @Test
    public void shouldGetSingleAlternationField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertEquals(VALUE, hash.get(FIELD_ALTERNATION));
    }

    @Test
    public void shouldGetSingleWildcardField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        Assertions.assertEquals(VALUE, hash.get(FIELD_WILDCARD));
    }

    @Test
    public void shouldGetMultipleCharacterClassFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(ALTERNATE_FIELD, OTHER_VALUE);

        assertArray(hash.get(FIELD_CHARACTER_CLASS));
    }

    @Test
    public void shouldGetMultipleAlternationFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(ALTERNATE_FIELD, OTHER_VALUE);

        assertArray(hash.get(FIELD_ALTERNATION));
    }

    @Test
    public void shouldGetMultipleWildcardFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(ALTERNATE_FIELD, OTHER_VALUE);

        assertArray(hash.get(FIELD_WILDCARD));
    }

    @Test
    public void shouldNotReplaceMissingField() {
        final Value.Hash hash = newHash();
        hash.replace(FIELD, VALUE);

        Assertions.assertNull(hash.get(FIELD));
        Assertions.assertFalse(hash.containsField(FIELD));
    }

    @Test
    public void shouldReplaceExistingField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.replace(FIELD, OTHER_VALUE);

        Assertions.assertEquals(OTHER_VALUE, hash.get(FIELD));
    }

    @Test
    public void shouldNotReplaceExistingFieldWithNullValue() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.replace(FIELD, (Value) null);

        Assertions.assertEquals(VALUE, hash.get(FIELD));
    }

    @Test
    public void shouldRemoveMissingField() {
        final Value.Hash hash = newHash();
        hash.remove(FIELD);

        Assertions.assertNull(hash.get(FIELD));
        Assertions.assertFalse(hash.containsField(FIELD));
    }

    @Test
    public void shouldRemoveExistingField() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.remove(FIELD);

        Assertions.assertNull(hash.get(FIELD));
        Assertions.assertFalse(hash.containsField(FIELD));
    }

    @Test
    public void shouldRemoveCharacterClassFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(ALTERNATE_FIELD, VALUE);
        hash.remove(FIELD_CHARACTER_CLASS);

        Assertions.assertTrue(hash.isEmpty());
    }

    @Test
    public void shouldRemoveAlternationFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(ALTERNATE_FIELD, VALUE);
        hash.remove(FIELD_ALTERNATION);

        Assertions.assertTrue(hash.isEmpty());
    }

    @Test
    public void shouldRemoveWildcardFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(ALTERNATE_FIELD, VALUE);
        hash.remove(FIELD_WILDCARD);

        Assertions.assertTrue(hash.isEmpty());
    }

    @Test
    public void shouldNotContainRemovedField() {
        final Value.Hash hash = newHash();
        Assertions.assertFalse(hash.containsField(FIELD));

        hash.put(FIELD, VALUE);
        Assertions.assertTrue(hash.containsField(FIELD));

        hash.remove(FIELD);
        Assertions.assertFalse(hash.containsField(FIELD));
    }

    @Test
    public void shouldRetainFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);

        hash.retainFields(Arrays.asList(FIELD));

        Assertions.assertTrue(hash.containsField(FIELD));
        Assertions.assertFalse(hash.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRetainNoFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        hash.retainFields(Arrays.asList());

        Assertions.assertTrue(hash.isEmpty());
    }

    @Test
    public void shouldNotRetainMissingFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);

        hash.retainFields(Arrays.asList(FIELD, OTHER_FIELD));

        Assertions.assertTrue(hash.containsField(FIELD));
        Assertions.assertFalse(hash.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRetainCharacterClassFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);

        hash.retainFields(Arrays.asList(FIELD_CHARACTER_CLASS));

        Assertions.assertTrue(hash.containsField(FIELD));
        Assertions.assertFalse(hash.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRetainAlternationFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);

        hash.retainFields(Arrays.asList(FIELD_ALTERNATION));

        Assertions.assertTrue(hash.containsField(FIELD));
        Assertions.assertFalse(hash.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRetainWildcardFields() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);

        hash.retainFields(Arrays.asList(FIELD_WILDCARD));

        Assertions.assertTrue(hash.containsField(FIELD));
        Assertions.assertFalse(hash.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRemoveEmptyValues() {
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, new Value(""));

        hash.removeEmptyValues();

        Assertions.assertTrue(hash.containsField(FIELD));
        Assertions.assertFalse(hash.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldIterateOverFieldValuePairs() {
        final Value emptyValue = new Value("");
        final Value specialValue = new Value("1");

        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);
        hash.put("empty field", emptyValue);
        hash.put("_special field", specialValue);

        MetafixTestHelpers.assertEmittedFields(hash,
                Arrays.asList(FIELD, OTHER_FIELD, "empty field", "_special field"),
                Arrays.asList(VALUE, OTHER_VALUE, emptyValue, specialValue)
        );
    }

    private void shouldFindArray(final String field) {
        final Value.Hash hash = newHash();
        hash.put(FIELD, Value.newArray(a -> a.add(VALUE)));

        Assertions.assertEquals(VALUE, new FixPath(String.join(".", FIELD, field)).findIn(hash));
    }

    @Test
    public void shouldFindArrayIndex() {
        shouldFindArray("1");
    }

    @Test
    public void shouldFindArrayWildcard() {
        shouldFindArray("$last");
    }

    private void shouldFindArraySubfield(final String field) {
        final Value.Hash hash = newHash();
        hash.put(FIELD, Value.newArray(a -> a.add(Value.newHash(h -> h.put(OTHER_FIELD, OTHER_VALUE)))));

        Assertions.assertEquals(OTHER_VALUE, new FixPath(String.join(".", FIELD, field, OTHER_FIELD)).findIn(hash));
    }

    @Test
    public void shouldFindArrayIndexSubfield() {
        shouldFindArraySubfield("1");
    }

    @Test
    public void shouldFindArrayWildcardSubfield() {
        shouldFindArraySubfield("$last");
    }

    private Value.Hash newHash() {
        return Value.newHash().asHash();
    }

    private void assertArray(final Value result) {
        Assertions.assertTrue(result.isArray());

        final Value.Array array = result.asArray();
        Assertions.assertEquals(2, array.size());
        Assertions.assertEquals(VALUE, array.get(0));
        Assertions.assertEquals(OTHER_VALUE, array.get(1));
    }

}
