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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

/**
 * Tests for class {@link WellformednessChecker}.
 *
 * @author Christoph Böhme
 *
 */
public final class WellformednessCheckerTest {

    @Mock
    private Consumer<String> errorHandler;

    private WellformednessChecker wellformednessChecker;

    public WellformednessCheckerTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        wellformednessChecker = new WellformednessChecker();
        wellformednessChecker.setErrorHandler(errorHandler);
    }

    @Test
    public void shouldAcceptValidStream() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.literal("literal1", "value1");
        wellformednessChecker.startEntity("entity1");
        wellformednessChecker.literal("literal2", "value2");
        wellformednessChecker.startEntity("entity2");
        wellformednessChecker.literal("literal3", "value3");
        wellformednessChecker.endEntity();
        wellformednessChecker.endEntity();
        wellformednessChecker.endRecord();
        wellformednessChecker.startRecord("id2");
        wellformednessChecker.startEntity("entity3");
        wellformednessChecker.literal("literal4", "value4");
        wellformednessChecker.endEntity();
        wellformednessChecker.endRecord();
        wellformednessChecker.closeStream();

        Mockito.verifyZeroInteractions(errorHandler);
    }

    @Test
    public void shouldAcceptEmptyStream() {
        wellformednessChecker.closeStream();

        Mockito.verifyZeroInteractions(errorHandler);
    }

    @Test
    public void shouldReportNullRecordId() {
        wellformednessChecker.startRecord(null);

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportNullEntityName() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.startEntity(null);

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportNullLiteralName() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.literal(null, "value1");

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldNotIgnoreStartRecordEventWithNullId() {
        wellformednessChecker.startRecord(null);
        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());

        wellformednessChecker.literal("literal", "value");
        wellformednessChecker.endRecord();

        Mockito.verifyZeroInteractions(errorHandler);
    }

    @Test
    public void shouldNotIgnoreStartEntityEventWithNullName() {
        wellformednessChecker.startRecord("id");
        wellformednessChecker.startEntity(null);
        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());

        wellformednessChecker.literal("literal", "value");
        wellformednessChecker.endEntity();
        wellformednessChecker.endRecord();

        Mockito.verifyZeroInteractions(errorHandler);
    }

    @Test
    public void shouldReportStartRecordInsideRecord() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.startRecord("id2");

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportEndRecordOutsideRecord() {
        wellformednessChecker.endRecord();

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportStartEntityOutsideRecord() {
        wellformednessChecker.startEntity("entity1");

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportEndEntityOutsideRecord() {
        wellformednessChecker.endEntity();

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportUnmatchedEndEntity() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.endEntity();

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportLiteralOutsideRecord() {
        wellformednessChecker.literal("literal1", "value1");

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportUnclosedRecord() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.closeStream();

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportUnclosedEntityAtEndRecord() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.startEntity("entity1");
        wellformednessChecker.endRecord();

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

    @Test
    public void shouldReportUnclosedEntityAtCloseStream() {
        wellformednessChecker.startRecord("id1");
        wellformednessChecker.startEntity("entity1");
        wellformednessChecker.closeStream();

        Mockito.verify(errorHandler).accept(ArgumentMatchers.any());
    }

}
