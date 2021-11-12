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
import java.util.List;

public class RecordTest {

    private static final String FIELD = "field";
    private static final String VALUE = "value";
    private static final String OTHER_VALUE = "other value";

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

        final List<String> list1 = new ArrayList<>();
        list1.add(VALUE);
        record.put(FIELD, list1);

        final Record clone = record.shallowClone();

        @SuppressWarnings("unchecked")
        final List<String> list2 = (List<String>) clone.get(FIELD);
        list2.add(OTHER_VALUE);

        Assertions.assertNotSame(record, clone);
        Assertions.assertSame(record.get(FIELD), clone.get(FIELD));

        @SuppressWarnings("unchecked")
        final List<String> list3 = (List<String>) clone.get(FIELD);
        Assertions.assertEquals(OTHER_VALUE, list3.get(1));
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

}
