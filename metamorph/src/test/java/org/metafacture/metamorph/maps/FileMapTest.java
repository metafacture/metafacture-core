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
package org.metafacture.metamorph.maps;

import static org.mockito.Mockito.inOrder;

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.InlineMorph;
import org.metafacture.metamorph.Metamorph;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link FileMap}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class FileMapTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldLookupValuesInFileBasedMap() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='1'>")
        .with("    <lookup in='map1' />")
        .with("  </data>")
        .with("</rules>")
        .with("<maps>")
        .with("  <filemap name='map1' files='org/metafacture/metamorph/maps/file-map-test.txt' />")
        .with("</maps>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("1", "gw");
    metamorph.literal("1", "fj");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("1", "Germany");
    ordered.verify(receiver).literal("1", "Fiji");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

    @Test
    public void shouldWhitelistValuesInFileBasedMap() {
        metamorph = InlineMorph.in(this)
                .with("<rules>")
                .with("  <data source='1'>")
                .with("    <whitelist map='map1' />")
                .with("  </data>")
                .with("</rules>")
                .with("<maps>")
                .with("  <filemap name='map1' files='org/metafacture/metamorph/maps/file-map-test.txt' />")
                .with("</maps>")
                .createConnectedTo(receiver);

        metamorph.startRecord("1");
        metamorph.literal("1", "gw");
        metamorph.literal("1", "fj");
        metamorph.literal("1", "bla");
        metamorph.endRecord();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("1", "gw");
        ordered.verify(receiver).literal("1", "fj");
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

}
