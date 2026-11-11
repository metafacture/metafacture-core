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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link RecordToEntity}.
 *
 * @author Christoph Böhme
 *
 */
public class RecordToEntityTest {

    @Mock
    private StreamReceiver receiver;

    private RecordToEntity recordToEntity;

    public RecordToEntityTest() {
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        recordToEntity = new RecordToEntity();
        recordToEntity.setReceiver(receiver);
    }

    @Test
    public void shouldReplaceRecordEventsWithEntityEvents() {
        recordToEntity.startRecord("1");
        recordToEntity.literal("literal", "value");
        recordToEntity.startEntity("entity");
        recordToEntity.endEntity();
        recordToEntity.endRecord();
        recordToEntity.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver)
                .startEntity(RecordToEntity.DEFAULT_ENTITY_NAME);
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver, Mockito.times(2)).endEntity();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void setEntityNameShouldChangeNameOfGeneratedEntity() {
        recordToEntity.setEntityName("container");

        recordToEntity.startRecord("1");
        recordToEntity.endRecord();
        recordToEntity.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startEntity("container");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void setIdLiteralNameShouldEnableOutputOfRecordIdAsLiteral() {
        recordToEntity.setIdLiteralName("record-id");

        recordToEntity.startRecord("1");
        receiver.endRecord();
        receiver.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver)
                .startEntity(RecordToEntity.DEFAULT_ENTITY_NAME);
        ordered.verify(receiver).literal("record-id", "1");
    }

}
