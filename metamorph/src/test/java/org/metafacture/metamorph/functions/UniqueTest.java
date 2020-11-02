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
package org.metafacture.metamorph.functions;

import static org.metafacture.metamorph.TestHelpers.assertMorph;

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Unique}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class UniqueTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  @Test
  public void shouldAllowSelectingTheUniqueScope() {
      assertMorph(receiver,
              "<rules>" +
              "  <data source='data' name='inRecord'>" +
              "    <unique />" +
              "  </data>" +
              "  <data source='e.data' name='inEntity'>" +
              "    <unique in='entity' />" +
              "  </data>" +
              "</rules>",
              i -> {
                  i.startRecord("1");
                  i.startEntity("e");
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.endEntity();
                  i.startEntity("e");
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.endEntity();
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.endRecord();
                  i.startRecord("2");
                  i.startEntity("e");
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.endEntity();
                  i.startEntity("e");
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.endEntity();
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.literal("data", "d");
                  i.endRecord();
              },
              (o, f) -> {
                  o.get().startRecord("1");
                  f.apply(2).literal("inEntity", "d");
                  o.get().literal("inRecord", "d");
                  o.get().endRecord();
                  o.get().startRecord("2");
                  f.apply(2).literal("inEntity", "d");
                  o.get().literal("inRecord", "d");
                  o.get().endRecord();
              }
      );
  }

  @Test
  public void shouldAllowSelectingTheUniquePart() {
      assertMorph(receiver,
              "<rules>" +
              "  <group name='name'>" +
              "    <group>" +
              "      <data source='data1' />" +
              "      <data source='data2' />" +
              "      <postprocess>" +
              "        <unique part='name' />" +
              "      </postprocess>" +
              "    </group>" +
              "  </group>" +
              "  <group name='value'>" +
              "    <group>" +
              "      <data source='data1' />" +
              "      <data source='data2' />" +
              "      <postprocess>" +
              "        <unique part='value' />" +
              "      </postprocess>" +
              "    </group>" +
              "  </group>" +
              "  <group name='both'>" +
              "    <group>" +
              "      <data source='data1' />" +
              "      <data source='data2' />" +
              "      <postprocess>" +
              "        <unique part='name-value' />" +
              "      </postprocess>" +
              "    </group>" +
              "  </group>" +
              "</rules>",
          i -> {
              i.startRecord("1");
              i.literal("data1", "d1");
              i.literal("data1", "d1");
              i.literal("data1", "d2");
              i.literal("data1", "d2");
              i.literal("data2", "d2");
              i.literal("data2", "d2");
              i.endRecord();
          },
          o -> {
              o.get().startRecord("1");
              o.get().literal("name", "d1");
              o.get().literal("value", "d1");
              o.get().literal("both", "d1");
              o.get().literal("value", "d2");
              o.get().literal("both", "d2");
              o.get().literal("name", "d2");
              o.get().literal("both", "d2");
              o.get().endRecord();
          }
      );
  }

}
