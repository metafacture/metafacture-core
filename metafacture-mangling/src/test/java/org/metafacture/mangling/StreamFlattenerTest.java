/*
 * Copyright 2016 Deutsche Nationalbibliothek
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

package org.metafacture.mangling;

import org.metafacture.framework.StreamReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamFlattener}.
 *
 * @author Christoph Böhme
 *
 */
public class StreamFlattenerTest {

    @Mock
    private StreamReceiver receiver;

    private StreamFlattener flattener;

    public StreamFlattenerTest() {
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        flattener = new StreamFlattener();
        flattener.setReceiver(receiver);
    }

    @Test
    public void shouldFlattenEntitiesAndUseEntityPathForLiteralNames() {
        flattener.startRecord("1");
        flattener.startEntity("granny");
        flattener.literal("me", "value1");
        flattener.startEntity("mommy");
        flattener.literal("myself", "value2");
        flattener.endEntity();
        flattener.endEntity();
        flattener.literal("andI", "value3");
        flattener.endRecord();
        flattener.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("granny.me", "value1");
        ordered.verify(receiver).literal("granny.mommy.myself", "value2");
        ordered.verify(receiver).literal("andI", "value3");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void getCurrentPathShouldReturnPathToCurrentEntity() {
        flattener.startRecord("1");
        Assert.assertEquals("", flattener.getCurrentPath());
        flattener.startEntity("granny");
        Assert.assertEquals("granny", flattener.getCurrentPath());
        flattener.literal("me", "value1");
        Assert.assertEquals("granny", flattener.getCurrentPath());
        flattener.startEntity("mommy");
        Assert.assertEquals("granny.mommy", flattener.getCurrentPath());
        flattener.literal("myself", "value2");
        Assert.assertEquals("granny.mommy", flattener.getCurrentPath());
        flattener.endEntity();
        Assert.assertEquals("granny", flattener.getCurrentPath());
        flattener.endEntity();
        Assert.assertEquals("", flattener.getCurrentPath());
        flattener.literal("andI", "value3");
        Assert.assertEquals("", flattener.getCurrentPath());
        flattener.endRecord();
        Assert.assertEquals("", flattener.getCurrentPath());
        flattener.closeStream();
    }

    @Test
    public void getCurrentEntityNameShouldReturnNameOfCurrentEntity() {
        flattener.startRecord("1");
        Assert.assertNull(flattener.getCurrentEntityName());
        flattener.startEntity("granny");
        Assert.assertEquals("granny", flattener.getCurrentEntityName());
        flattener.literal("me", "value1");
        Assert.assertEquals("granny", flattener.getCurrentEntityName());
        flattener.startEntity("mommy");
        Assert.assertEquals("mommy", flattener.getCurrentEntityName());
        flattener.literal("myself", "value2");
        Assert.assertEquals("mommy", flattener.getCurrentEntityName());
        flattener.endEntity();
        Assert.assertEquals("granny", flattener.getCurrentEntityName());
        flattener.endEntity();
        Assert.assertNull(flattener.getCurrentEntityName());
        flattener.literal("andI", "value3");
        Assert.assertNull(flattener.getCurrentEntityName());
        flattener.endRecord();
        Assert.assertNull(flattener.getCurrentEntityName());
        flattener.closeStream();
    }

    @Test
    public void setEntityMarkerShouldChangeMarkerBetweenEntities() {
        flattener.setEntityMarker("-");

        flattener.startRecord("1");
        flattener.startEntity("granny");
        flattener.startEntity("mommy");
        Assert.assertEquals("granny-mommy", flattener.getCurrentPath());
    }

}
