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

package org.metafacture.linkeddata;

import org.metafacture.framework.StreamReceiver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link OreAggregationAdder}
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class OreAggregationAdderTest {

    @Mock
    private StreamReceiver receiver;

    private OreAggregationAdder oreAggregationAdder;

    public OreAggregationAdderTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        oreAggregationAdder = new OreAggregationAdder();
        oreAggregationAdder.setReceiver(receiver);
    }

    @Test
    public void shouldAddWebResource() {
        oreAggregationAdder.startRecord("1");
        oreAggregationAdder.literal("copyright", "CC-0");
        oreAggregationAdder.literal("aggregation_id", "hawaii_aggregation");
        oreAggregationAdder.startEntity("edm:WebResource");
        oreAggregationAdder.literal("~rdf:about", "hawaii");
        oreAggregationAdder.literal("hula", "hula");
        oreAggregationAdder.endEntity();
        oreAggregationAdder.endRecord();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("edm:WebResource");
        ordered.verify(receiver).literal("~rdf:about", "hawaii");
        ordered.verify(receiver).literal("hula", "hula");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("ore:Aggregation");
        ordered.verify(receiver).literal("~rdf:about", "hawaii_aggregation");
        ordered.verify(receiver).startEntity("edm:isShownBy");
        ordered.verify(receiver).literal("~rdf:resource", "hawaii");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).literal("copyright", "CC-0");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

}
