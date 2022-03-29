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
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

@ExtendWith(MetafixToDo.Extension.class)
public class RecordTest {

    private static final String FIELD = "field";
    private static final String OTHER_FIELD = "other field";

    private static final Value VALUE = new Value("value");
    private static final Value OTHER_VALUE = new Value("other value");

    public RecordTest() {
    }

    @Test
    public void shouldCreateShallowCloneFromEmptyRecord() {
        final Record record = new Record();
        final Record clone = record.shallowClone();

        Assertions.assertNotSame(record, clone);
    }

    @Test
    public void shouldCreateShallowCloneFromNonEmptyRecord() {
        final Record record = new Record();
        record.put(FIELD, VALUE);

        final Record clone = record.shallowClone();

        Assertions.assertNotSame(record, clone);
        Assertions.assertSame(record.get(FIELD), clone.get(FIELD));
    }

    @Test
    public void shouldNotModifyTopLevelFromShallowClone() {
        final Record record = new Record();

        final Record clone = record.shallowClone();
        clone.put(FIELD, VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertNull(record.get(FIELD));
        Assertions.assertNotNull(clone.get(FIELD));
    }

    @Test
    public void shouldNotModifyOverwrittenValueFromShallowClone() {
        final Record record = new Record();
        record.put(FIELD, VALUE);

        final Record clone = record.shallowClone();
        clone.put(FIELD, OTHER_VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertEquals(VALUE, record.get(FIELD));
        Assertions.assertEquals(OTHER_VALUE, clone.get(FIELD));
    }

    @Test
    public void shouldModifySubLevelFromShallowClone() {
        final Record record = new Record();
        record.put(FIELD, Value.newArray(a -> a.add(VALUE)));

        final Record clone = record.shallowClone();
        clone.get(FIELD).asArray().add(OTHER_VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertSame(record.get(FIELD), clone.get(FIELD));

        final Value.Array array = clone.get(FIELD).asArray();
        Assertions.assertEquals(OTHER_VALUE, array.get(1));
    }

    @Test
    public void shouldEmitRecordByDefault() {
        final Record record = new Record();
        Assertions.assertFalse(record.getReject());
    }

    @Test
    public void shouldNotEmitRecordIfRejected() {
        final Record record = new Record();
        record.setReject(true);

        Assertions.assertTrue(record.getReject());
    }

    @Test
    public void shouldEmitCloneOfDefaultRecord() {
        final Record record = new Record();
        final Record clone = record.shallowClone();

        Assertions.assertFalse(clone.getReject());
    }

    @Test
    public void shouldNotEmitCloneOfRejectedRecord() {
        final Record record = new Record();
        record.setReject(true);

        final Record clone = record.shallowClone();

        Assertions.assertTrue(clone.getReject());
    }

    @Test
    public void shouldNotEmitCloneOfDefaultRecordIfRejected() {
        final Record record = new Record();

        final Record clone = record.shallowClone();
        clone.setReject(true);

        Assertions.assertFalse(record.getReject());
        Assertions.assertTrue(clone.getReject());
    }

    @Test
    public void shouldNotContainMissingVirtualField() {
        final Record record = new Record();
        Assertions.assertFalse(record.containsVirtualField(FIELD));
    }

    @Test
    public void shouldContainExistingVirtualField() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        Assertions.assertTrue(record.containsVirtualField(FIELD));
    }

    @Test
    public void shouldNotContainVirtualFieldWithNullValue() {
        final Record record = new Record();
        record.putVirtualField(FIELD, null);

        Assertions.assertFalse(record.containsVirtualField(FIELD));
    }

    @Test
    public void shouldGetVirtualField() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        Assertions.assertEquals(VALUE, record.get(FIELD));
    }

    @Test
    public void shouldGetRegularFieldInsteadOfVirtualField() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        record.put(FIELD, OTHER_VALUE);

        Assertions.assertEquals(OTHER_VALUE, record.get(FIELD));
    }

    @Test
    public void shouldNotEmitVirtualFieldsByDefault() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        MetafixTestHelpers.assertEmittedFields(record, Arrays.asList(), Arrays.asList());
    }

    @Test
    public void shouldEmitVirtualFieldsWhenRetained() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        record.retainFields(Arrays.asList(FIELD));

        MetafixTestHelpers.assertEmittedFields(record, Arrays.asList(FIELD), Arrays.asList(VALUE));
    }

    @Test
    public void shouldEmitVirtualFieldsWhenCopied() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        record.put(FIELD, record.get(FIELD));

        MetafixTestHelpers.assertEmittedFields(record, Arrays.asList(FIELD), Arrays.asList(VALUE));
    }

    @Test
    public void shouldEmitVirtualFieldsWhenAdded() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        record.put(FIELD, OTHER_VALUE);

        MetafixTestHelpers.assertEmittedFields(record, Arrays.asList(FIELD), Arrays.asList(OTHER_VALUE));
    }

    @Test
    public void shouldCreateShallowCloneFromRecordWithVirtualFields() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        final Record clone = record.shallowClone();

        Assertions.assertNotSame(record, clone);
        Assertions.assertSame(record.get(FIELD), clone.get(FIELD));
    }

    @Test
    public void shouldNotModifyTopLevelFromShallowCloneWithVirtualFields() {
        final Record record = new Record();

        final Record clone = record.shallowClone();
        clone.putVirtualField(FIELD, VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertNull(record.get(FIELD));
        Assertions.assertNotNull(clone.get(FIELD));
    }

    @Test
    public void shouldNotModifyOverwrittenValueFromShallowCloneWithVirtualFields() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        final Record clone = record.shallowClone();
        clone.putVirtualField(FIELD, OTHER_VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertEquals(VALUE, record.get(FIELD));
        Assertions.assertEquals(OTHER_VALUE, clone.get(FIELD));
    }

    @Test
    public void shouldModifySubLevelFromShallowCloneWithVirtualFields() {
        final Record record = new Record();
        record.putVirtualField(FIELD, Value.newArray(a -> a.add(VALUE)));

        final Record clone = record.shallowClone();
        clone.get(FIELD).asArray().add(OTHER_VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertSame(record.get(FIELD), clone.get(FIELD));

        final Value.Array array = clone.get(FIELD).asArray();
        Assertions.assertEquals(OTHER_VALUE, array.get(1));
    }

    @Test
    public void shouldNotEmitVirtualFieldsFromShallowClone() {
        final Record record = new Record();
        record.putVirtualField(FIELD, VALUE);

        final Record clone = record.shallowClone();
        MetafixTestHelpers.assertEmittedFields(clone, Arrays.asList(), Arrays.asList());
    }

    private void shouldFindArrayIndexSubfield(final Consumer<Record> consumer) {
        final Record record = new Record();
        record.put(FIELD, Value.newArray(a -> a.add(Value.newHash(h -> {
            h.put(FIELD, VALUE);
            h.put(OTHER_FIELD, OTHER_VALUE);
        }))));

        if (consumer != null) {
            consumer.accept(record);
        }

        Assertions.assertEquals(VALUE, record.get(String.join(".", FIELD, "1", FIELD)));
    }

    @Test
    public void shouldFindArrayIndexSubfield() {
        shouldFindArrayIndexSubfield(null);
    }

    @Test
    public void shouldFindArrayIndexSubfieldAfterTransformingOtherSubfield() {
        shouldFindArrayIndexSubfield(record -> {
            final String path = String.join(".", FIELD, "1", OTHER_FIELD);
            final String value =  "transformed value";

            record.transform(path, s -> value);
            Assertions.assertEquals(new Value(value), record.get(path));
        });
    }

    @Test
    public void shouldFindArrayIndexSubfieldAfterTransformingOtherSubfieldToNull() {
        shouldFindArrayIndexSubfield(record -> {
            final String path = String.join(".", FIELD, "1", OTHER_FIELD);
            final String value = null;

            record.transform(path, s -> value);
            Assertions.assertEquals(value, record.get(path));
        });
    }

    @Test
    public void shouldPreserveOrderWhenTransformingArraySubfields() {
        final Record record = new Record();
        record.put(FIELD, Value.newArray(a -> {
            for (int i = 0; i < 3; ++i) {
                a.add(Value.newHash(h -> {
                    h.put(FIELD, VALUE);
                    h.put(OTHER_FIELD, OTHER_VALUE);
                }));
            }
        }));

        final String path = String.join(".", FIELD, "%s", OTHER_FIELD);

        final LongAdder counter = new LongAdder();
        record.transform(String.format(path, "*"), s -> {
            counter.increment();
            return s + counter;
        });

        Assertions.assertEquals(new Value(OTHER_VALUE + "1"), record.get(String.format(path, 1)));
        Assertions.assertEquals(new Value(OTHER_VALUE + "2"), record.get(String.format(path, 2)));
        Assertions.assertEquals(new Value(OTHER_VALUE + "3"), record.get(String.format(path, 3)));
    }

    @Test
    @MetafixToDo("See https://github.com/metafacture/metafacture-fix/pull/170")
    public void shouldPreserveOrderWhenTransformingOptionalArraySubfield() {
        final Record record = new Record();
        record.put(FIELD, Value.newArray(a -> {
            a.add(Value.newHash(h -> {
                h.put(FIELD, VALUE);
            }));
            a.add(Value.newHash(h -> {
                h.put(FIELD, VALUE);
                h.put(OTHER_FIELD, OTHER_VALUE);
            }));
        }));

        final String path = String.join(".", FIELD, "%s", OTHER_FIELD);
        final String value = "transformed value";

        record.transform(String.format(path, "*"), s -> value);

        Assertions.assertEquals(null, record.get(String.format(path, 1)));
        Assertions.assertEquals(new Value(value), record.get(String.format(path, 2)));
    }

}
