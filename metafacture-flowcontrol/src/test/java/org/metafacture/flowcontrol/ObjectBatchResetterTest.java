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

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectReceiver;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link ObjectBatchResetter}.
 */
public class ObjectBatchResetterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectReceiver<String> receiver;

    private ObjectBatchResetter<String> systemUnderTest;

    public ObjectBatchResetterTest() {
    }

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

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process("1");
        ordered.verify(receiver).process("2");
        ordered.verify(receiver).process("3");
        ordered.verify(receiver).resetStream();
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldIncreaseObjectCounterAfterEachObject() {
        systemUnderTest.setBatchSize(3);

        systemUnderTest.process("1");
        systemUnderTest.process("2");

        Assertions.assertThat(systemUnderTest.getObjectCount())
                .isEqualTo(2);
    }

    @Test
    public void shouldResetObjectCountOnBatchCompletion() {
        systemUnderTest.setBatchSize(2);

        systemUnderTest.process("1");
        systemUnderTest.process("2");

        Assertions.assertThat(systemUnderTest.getObjectCount())
                .isZero();
    }

    @Test
    public void shouldIncreaseBatchCountAfterEachBatch() {
        systemUnderTest.setBatchSize(2);

        systemUnderTest.process("1");
        systemUnderTest.process("2");
        systemUnderTest.process("3");
        systemUnderTest.process("4");

        Assertions.assertThat(systemUnderTest.getBatchCount())
                .isEqualTo(2);
    }

    @Test
    public void shouldResetCountsOnResetStream() {
        systemUnderTest.setBatchSize(2);

        systemUnderTest.process("1");
        systemUnderTest.process("2");
        systemUnderTest.process("3");
        systemUnderTest.resetStream();

        Assertions.assertThat(systemUnderTest.getBatchCount())
                .isZero();
        Assertions.assertThat(systemUnderTest.getObjectCount())
                .isZero();
    }

    @Test
    public void shouldEmitResetStreamEventAfterUpdatingCounts() {
        systemUnderTest.setBatchSize(2);
        systemUnderTest.setReceiver(new DefaultObjectReceiver<String>() {
            @Override
            public void resetStream() {
                Assertions.assertThat(systemUnderTest.getObjectCount()).isZero();
                Assertions.assertThat(systemUnderTest.getBatchCount()).isEqualTo(1);
            }
        });

        systemUnderTest.process("1");
        systemUnderTest.process("2");
    }

}
