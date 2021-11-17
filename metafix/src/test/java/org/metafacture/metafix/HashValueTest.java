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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HashValueTest {

    private static final String FIELD = "field";
    private static final String OTHER_FIELD = "other field";

    private static final Value VALUE = new Value("value");
    private static final Value OTHER_VALUE = new Value("other value");

    public HashValueTest() {
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
        final Value.Hash hash = newHash();
        hash.put(FIELD, VALUE);
        hash.put(OTHER_FIELD, OTHER_VALUE);
        hash.put("empty field", new Value(""));
        hash.put("_special field", new Value("1"));

        final List<String> fields = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        hash.forEach((f, v) -> {
            fields.add(f);
            values.add(v.asString());
        });

        Assertions.assertEquals(Arrays.asList(FIELD, OTHER_FIELD, "empty field", "_special field"), fields);
        Assertions.assertEquals(Arrays.asList(VALUE.asString(), OTHER_VALUE.asString(), "", "1"), values);
    }

    private Value.Hash newHash() {
        return Value.newHash().asHash();
    }

}
