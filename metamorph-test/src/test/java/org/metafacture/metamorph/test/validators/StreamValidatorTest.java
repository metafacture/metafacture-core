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

package org.metafacture.metamorph.test.validators;

import org.metafacture.javaintegration.EventList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

/**
 * Tests for class {@link StreamValidator}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamValidatorTest {

    @Mock
    private Consumer<String> errorHandler;

    public StreamValidatorTest() {
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldFailIfEndRecordEventIsMissing() {
        final EventList stream = new EventList();
        stream.startRecord("1");
        stream.endRecord();
        stream.closeStream();

        final StreamValidator validator = new StreamValidator(stream.getEvents());
        validator.setErrorHandler(errorHandler);

        validator.startRecord("1");
        validator.closeStream();

        Mockito.verify(errorHandler, Mockito.atLeastOnce()).accept(ArgumentMatchers.any());
    }

}
