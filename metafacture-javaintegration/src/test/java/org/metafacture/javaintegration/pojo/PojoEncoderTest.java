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

package org.metafacture.javaintegration.pojo;

import org.metafacture.framework.ObjectReceiver;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

/**
 * Tests for class {@link PojoEncoder}.
 *
 * @author Thomas Seidel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public class PojoEncoderTest {

    public PojoEncoderTest() {
    }

    @Test
    public void shouldEncodeEmptyEntityStreamToEmptyPojo() {
        final ObjectReceiver<EmptyPojo> receiver = createObjectReceiverMock();
        final PojoEncoder<EmptyPojo> pojoEncoder = new PojoEncoder<>(
                EmptyPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.endRecord();

        Mockito.verify(receiver).process(ArgumentMatchers.any(EmptyPojo.class));
    }

    @Test
    public void shouldEncodeEntityStreamToSimplePojo() {
        final ObjectReceiver<SimplePojo> receiver = createObjectReceiverMock();
        final PojoEncoder<SimplePojo> pojoEncoder = new PojoEncoder<>(
                SimplePojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.literal("stringField1", "value1");
        pojoEncoder.literal("stringField2", "value2");
        pojoEncoder.literal("intField1", "42");
        pojoEncoder.literal("intField2", "23");
        pojoEncoder.endRecord();

        final ArgumentCaptor<SimplePojo> objectCaptor =
                ArgumentCaptor.forClass(SimplePojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final SimplePojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertEquals("value1", encodedPojo.stringField1);
        Assert.assertEquals("value2", encodedPojo.getStringField2());
        Assert.assertEquals(42, encodedPojo.intField1);
        Assert.assertEquals(23, encodedPojo.getIntField2());
    }

    @Test
    public void shouldEncodeEntityStreamToDoubleNestedPojo() {
        final ObjectReceiver<DoubleNestedPojo> receiver =
                createObjectReceiverMock();
        final PojoEncoder<DoubleNestedPojo> pojoEncoder =
                new PojoEncoder<>(DoubleNestedPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.startEntity("nestedPojo");
        pojoEncoder.startEntity("simplePojo");
        pojoEncoder.literal("stringField1", "value1");
        pojoEncoder.literal("stringField2", "value2");
        pojoEncoder.endEntity();
        pojoEncoder.literal("attribute", "value3");
        pojoEncoder.endEntity();
        pojoEncoder.endRecord();

        final ArgumentCaptor<DoubleNestedPojo> objectCaptor =
                ArgumentCaptor.forClass(DoubleNestedPojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final DoubleNestedPojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertNotNull(encodedPojo.nestedPojo);
        Assert.assertEquals("value3", encodedPojo.nestedPojo.attribute);
        Assert.assertNotNull(encodedPojo.nestedPojo.simplePojo);
        Assert.assertEquals("value1", encodedPojo.nestedPojo.simplePojo.stringField1);
        Assert.assertEquals("value2", encodedPojo.nestedPojo.simplePojo.getStringField2());
    }

    @Test
    public void shouldEncodeEntityStreamToPojoWithStringList() {
        final ObjectReceiver<StringListPojo> receiver = createObjectReceiverMock();
        final PojoEncoder<StringListPojo> pojoEncoder =
                new PojoEncoder<>(StringListPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.startEntity("stringList");
        pojoEncoder.literal("firstElement", "value1");
        pojoEncoder.literal("secondElement", "value2");
        pojoEncoder.endEntity();
        pojoEncoder.literal("attribute", "value3");
        pojoEncoder.endRecord();

        final ArgumentCaptor<StringListPojo> objectCaptor =
                ArgumentCaptor.forClass(StringListPojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final StringListPojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertEquals("value3", encodedPojo.attribute);
        Assert.assertNotNull(encodedPojo.stringList);
        Assert.assertEquals(2, encodedPojo.stringList.size());
        Assert.assertEquals("value1", encodedPojo.stringList.get(0));
        Assert.assertEquals("value2", encodedPojo.stringList.get(1));
    }

    @Test
    public void shouldEncodeEntityStreamToPojoWithIntegerList() {
        final ObjectReceiver<IntegerListPojo> receiver = createObjectReceiverMock();
        final PojoEncoder<IntegerListPojo> pojoEncoder =
                new PojoEncoder<>(IntegerListPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.startEntity("integerList");
        pojoEncoder.literal("firstElement", "42");
        pojoEncoder.literal("firstElement", "23");
        pojoEncoder.endEntity();
        pojoEncoder.endRecord();

        final ArgumentCaptor<IntegerListPojo> objectCaptor =
                ArgumentCaptor.forClass(IntegerListPojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final IntegerListPojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertNotNull(encodedPojo.integerList);
        Assert.assertEquals(2, encodedPojo.integerList.size());
        Assert.assertEquals(42, encodedPojo.integerList.get(0).intValue());
        Assert.assertEquals(23, encodedPojo.integerList.get(1).intValue());
    }

    @Test
    public void shouldEncodeEntityStreamToPojoWithSimplePojoList() {
        final ObjectReceiver<SimplePojoListPojo> receiver =
                createObjectReceiverMock();
        final PojoEncoder<SimplePojoListPojo> pojoEncoder =
                new PojoEncoder<>(SimplePojoListPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.startEntity("simplePojoList");
        pojoEncoder.startEntity("simplePojo");
        pojoEncoder.literal("stringField1", "value1");
        pojoEncoder.literal("stringField2", "value2");
        pojoEncoder.endEntity();
        pojoEncoder.startEntity("simplePojo");
        pojoEncoder.literal("stringField1", "value3");
        pojoEncoder.endEntity();
        pojoEncoder.endEntity();
        pojoEncoder.endRecord();

        final ArgumentCaptor<SimplePojoListPojo> objectCaptor =
                ArgumentCaptor.forClass(SimplePojoListPojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final SimplePojoListPojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertNotNull(encodedPojo.simplePojoList);
        Assert.assertEquals(2, encodedPojo.simplePojoList.size());
        Assert.assertEquals("value1", encodedPojo.simplePojoList.get(0).stringField1);
        Assert.assertEquals("value2", encodedPojo.simplePojoList.get(0).getStringField2());
        Assert.assertEquals("value3", encodedPojo.simplePojoList.get(1).stringField1);
    }

    @Test
    public void shouldEncodeEntityStreamToPojoWithStringMap() {
        final ObjectReceiver<StringMapPojo> receiver =
                createObjectReceiverMock();
        final PojoEncoder<StringMapPojo> pojoEncoder =
                new PojoEncoder<StringMapPojo>(StringMapPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.startEntity("stringMap");
        pojoEncoder.literal("mapKey1", "mapValue1");
        pojoEncoder.literal("mapKey2", "mapValue2");
        pojoEncoder.endEntity();
        pojoEncoder.endRecord();

        final ArgumentCaptor<StringMapPojo> objectCaptor =
                ArgumentCaptor.forClass(StringMapPojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final StringMapPojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertNotNull(encodedPojo.stringMap);
        Assert.assertEquals(2, encodedPojo.stringMap.size());
        Assert.assertEquals("mapValue1", encodedPojo.stringMap.get("mapKey1"));
        Assert.assertEquals("mapValue2", encodedPojo.stringMap.get("mapKey2"));
    }

    @Test
    public void shouldEncodeEntityStreamToPojoWithSimplePojoMap() {
        final ObjectReceiver<SimplePojoMapPojo> receiver =
                createObjectReceiverMock();
        final PojoEncoder<SimplePojoMapPojo> pojoEncoder =
                new PojoEncoder<>(SimplePojoMapPojo.class);
        pojoEncoder.setReceiver(receiver);

        pojoEncoder.startRecord("identifier");
        pojoEncoder.startEntity("simplePojoMap");
        pojoEncoder.startEntity("mapKeyA");
        pojoEncoder.literal("stringField1", "stringValueA1");
        pojoEncoder.literal("stringField2", "stringValueA2");
        pojoEncoder.endEntity();
        pojoEncoder.startEntity("mapKeyB");
        pojoEncoder.literal("stringField1", "stringValueB1");
        pojoEncoder.literal("stringField2", "stringValueB2");
        pojoEncoder.endEntity();
        pojoEncoder.endEntity();
        pojoEncoder.endRecord();

        final ArgumentCaptor<SimplePojoMapPojo> objectCaptor =
                ArgumentCaptor.forClass(SimplePojoMapPojo.class);
        Mockito.verify(receiver).process(objectCaptor.capture());
        final SimplePojoMapPojo encodedPojo = objectCaptor.getValue();
        Assert.assertNotNull(encodedPojo);
        Assert.assertNotNull(encodedPojo.simplePojoMap);
        Assert.assertEquals(2, encodedPojo.simplePojoMap.size());
        Assert.assertEquals("stringValueA1", encodedPojo.simplePojoMap.get("mapKeyA")
                .stringField1);
        Assert.assertEquals("stringValueA2", encodedPojo.simplePojoMap.get("mapKeyA")
                .stringField2);
        Assert.assertEquals("stringValueB1", encodedPojo.simplePojoMap.get("mapKeyB")
                .stringField1);
        Assert.assertEquals("stringValueB2", encodedPojo.simplePojoMap.get("mapKeyB")
                .stringField2);
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectReceiver<T> createObjectReceiverMock() {
        // There is no type safe to create a mock with Mockito#mock(Class).
        // Hence, we have to use an unchecked cast here:
        return (ObjectReceiver<T>) Mockito.mock(ObjectReceiver.class);
    }

    public static class EmptyPojo {

        public EmptyPojo() {
        }

    }

    public static class SimplePojo {

        public String stringField1; // checkstyle-disable-line VisibilityModifier
        public int intField1; // checkstyle-disable-line VisibilityModifier

        private String stringField2;
        private int intField2;

        public SimplePojo() {
        }

        public void setStringField2(final String stringField2) {
            this.stringField2 = stringField2;
        }

        public String getStringField2() {
            return stringField2;
        }

        public void setIntField2(final int intField2) {
            this.intField2 = intField2;
        }

        public int getIntField2() {
            return intField2;
        }

    }

    public static class NestedPojo {

        public String attribute; // checkstyle-disable-line VisibilityModifier
        public SimplePojo simplePojo; // checkstyle-disable-line VisibilityModifier

        public NestedPojo() {
        }

    }

    public static class DoubleNestedPojo {

        public NestedPojo nestedPojo; // checkstyle-disable-line VisibilityModifier

        public DoubleNestedPojo() {
        }

    }

    public static class StringListPojo {

        public List<String> stringList; // checkstyle-disable-line VisibilityModifier
        public String attribute; // checkstyle-disable-line VisibilityModifier

        public StringListPojo() {
        }

    }

    public static class IntegerListPojo {

        public List<Integer> integerList; // checkstyle-disable-line VisibilityModifier

        public IntegerListPojo() {
        }

    }

    public static class SimplePojoListPojo {

        private List<SimplePojo> simplePojoList;

        public SimplePojoListPojo() {
        }

        public void setSimplePojoList(final List<SimplePojo> simplePojoList) {
            this.simplePojoList = simplePojoList;
        }

    }

    public static class StringMapPojo {

        public Map<String, String> stringMap; // checkstyle-disable-line VisibilityModifier

        public StringMapPojo() {
        }

    }

    public static class SimplePojoMapPojo {

        public Map<String, SimplePojo> simplePojoMap; // checkstyle-disable-line VisibilityModifier

        public SimplePojoMapPojo() {
        }

    }

}
