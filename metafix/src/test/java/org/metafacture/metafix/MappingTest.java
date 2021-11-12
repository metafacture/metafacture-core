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

public class MappingTest {

    private static final String FIELD = "field";
    private static final String OTHER_FIELD = "other field";
    private static final String VALUE = "value";
    private static final String OTHER_VALUE = "other value";

    public MappingTest() {
    }

    @Test
    public void shouldNotContainMissingField() {
        final Mapping mapping = new Mapping();
        Assertions.assertFalse(mapping.containsField(FIELD));
    }

    @Test
    public void shouldContainExistingField() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);

        Assertions.assertTrue(mapping.containsField(FIELD));
    }

    @Test
    public void shouldNotContainNullValue() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, null);

        Assertions.assertFalse(mapping.containsField(FIELD));
    }

    @Test
    public void shouldBeEmptyByDefault() {
        final Mapping mapping = new Mapping();
        Assertions.assertTrue(mapping.isEmpty());
    }

    @Test
    public void shouldNotBeEmptyAfterAddingValue() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);

        Assertions.assertFalse(mapping.isEmpty());
    }

    @Test
    public void shouldNotAddNullValue() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, null);

        Assertions.assertTrue(mapping.isEmpty());
    }

    @Test
    public void shouldGetSizeOfDefaultMapping() {
        final Mapping mapping = new Mapping();
        Assertions.assertEquals(0, mapping.size());
    }

    @Test
    public void shouldGetSizeAfterAddingValues() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.put(OTHER_FIELD, OTHER_VALUE);

        Assertions.assertEquals(2, mapping.size());
    }

    @Test
    public void shouldNotGetMissingField() {
        final Mapping mapping = new Mapping();
        Assertions.assertNull(mapping.get(FIELD));
    }

    @Test
    public void shouldGetExistingField() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);

        Assertions.assertEquals(VALUE, mapping.get(FIELD));
    }

    @Test
    public void shouldNotReplaceMissingField() {
        final Mapping mapping = new Mapping();
        mapping.replace(FIELD, VALUE);

        Assertions.assertNull(mapping.get(FIELD));
        Assertions.assertFalse(mapping.containsField(FIELD));
    }

    @Test
    public void shouldReplaceExistingField() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.replace(FIELD, OTHER_VALUE);

        Assertions.assertEquals(OTHER_VALUE, mapping.get(FIELD));
    }

    @Test
    public void shouldNotReplaceExistingFieldWithNullValue() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.replace(FIELD, null);

        Assertions.assertEquals(VALUE, mapping.get(FIELD));
    }

    @Test
    public void shouldRemoveMissingField() {
        final Mapping mapping = new Mapping();
        mapping.remove(FIELD);

        Assertions.assertNull(mapping.get(FIELD));
        Assertions.assertFalse(mapping.containsField(FIELD));
    }

    @Test
    public void shouldRemoveExistingField() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.remove(FIELD);

        Assertions.assertNull(mapping.get(FIELD));
        Assertions.assertFalse(mapping.containsField(FIELD));
    }

    @Test
    public void shouldRetainFields() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.put(OTHER_FIELD, OTHER_VALUE);

        mapping.retainFields(Arrays.asList(FIELD));

        Assertions.assertTrue(mapping.containsField(FIELD));
        Assertions.assertFalse(mapping.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRetainNoFields() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);

        mapping.retainFields(Arrays.asList());

        Assertions.assertTrue(mapping.isEmpty());
    }

    @Test
    public void shouldNotRetainMissingFields() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);

        mapping.retainFields(Arrays.asList(FIELD, OTHER_FIELD));

        Assertions.assertTrue(mapping.containsField(FIELD));
        Assertions.assertFalse(mapping.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldRemoveEmptyValues() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.put(OTHER_FIELD, "");

        mapping.removeEmptyValues();

        Assertions.assertTrue(mapping.containsField(FIELD));
        Assertions.assertFalse(mapping.containsField(OTHER_FIELD));
    }

    @Test
    public void shouldIterateOverFieldValuePairs() {
        final Mapping mapping = new Mapping();
        mapping.put(FIELD, VALUE);
        mapping.put(OTHER_FIELD, OTHER_VALUE);
        mapping.put("empty field", "");
        mapping.put("_special field", 1);

        final List<String> fields = new ArrayList<>();
        final List<Object> values = new ArrayList<>();
        mapping.forEach((k, v) -> {
            fields.add(k);
            values.add(v);
        });

        Assertions.assertEquals(Arrays.asList(FIELD, OTHER_FIELD, "empty field", "_special field"), fields);
        Assertions.assertEquals(Arrays.asList(VALUE, OTHER_VALUE, "", 1), values);
    }

}
