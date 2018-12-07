/*
 * Copyright 2018 Deutsche Nationalbibliothek
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
package org.metafacture.flowcontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link ObjectBatchResetter}.
 */
public class ObjectBatchResetterTest {

    private ObjectBatchResetter<String> systemUnderTest;

    @Before
    public void setupSystemUnderTest() {

        systemUnderTest = new ObjectBatchResetter<>();
        systemUnderTest.setReceiver(receiver);
    }

    @Test
    public void shouldEmitResetStreamAfterBatchSizeObjects() {

        systemUnderTest.setBatchSize(3);

        systemUnderTest.process("1");
        systemUnderTest.process("2");
        systemUnderTest.process("3");

        InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("1");
        ordered.verify(receiver).process("2");
        ordered.verify(receiver).process("3");
        ordered.verify(receiver).resetStream();
        verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldIncreaseObjectCounterAfterEachObject() {

        systemUnderTest.setBatchSize(3);

        systemUnderTest.process("1");
        systemUnderTest.process("2");

        assertThat(systemUnderTest.getObjectCount())
                .isEqualTo(2);
    }

    @Test
    public void shouldResetObjectCountOnBatchCompletion() {

        systemUnderTest.setBatchSize(2);

        systemUnderTest.process("1");
        systemUnderTest.process("2");

        assertThat(systemUnderTest.getObjectCount())
                .isZero();
    }

    @Test
    public void shouldIncreaseBatchCountAfterEachBatch() {

        systemUnderTest.setBatchSize(2);

        systemUnderTest.process("1");
        systemUnderTest.process("2");
        systemUnderTest.process("3");
        systemUnderTest.process("4");

        assertThat(systemUnderTest.getBatchCount())
                .isEqualTo(2);
    }

    @Test
    public void shouldResetCountsOnResetStream() {

        systemUnderTest.setBatchSize(2);

        systemUnderTest.process("1");
        systemUnderTest.process("2");
        systemUnderTest.process("3");
        systemUnderTest.resetStream();

        assertThat(systemUnderTest.getBatchCount())
                .isZero();
        assertThat(systemUnderTest.getObjectCount())
                .isZero();
    }

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectReceiver<String> receiver;

}
