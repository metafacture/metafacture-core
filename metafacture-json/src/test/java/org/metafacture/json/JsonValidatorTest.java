/*
 * Copyright 2021, 2022 Fabian Steeg, hbz
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
package org.metafacture.json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link JsonValidator}.
 *
 * @author Fabian Steeg
 *
 */
public final class JsonValidatorTest {

    private static final String SCHEMA = "/schemas/schema.json";
    private static final String JSON_VALID = "{\"id\":\"http://example.org/\"}";
    private static final String JSON_INVALID_MISSING_REQUIRED = "{}";
    private static final String JSON_INVALID_URI_FORMAT= "{\"id\":\"example.org/\"}";
    private static final String JSON_INVALID_DUPLICATE_KEY = "{\"id\":\"val\",\"id\":\"val\"}";
    private static final String JSON_INVALID_SYNTAX_ERROR = "{\"id1\":\"val\",\"id2\":\"val\"";

    private JsonValidator validator;

    @Mock
    private ObjectReceiver<String> receiver;
    private InOrder inOrder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new JsonValidator(SCHEMA);
        validator.setSchemaRoot("/schemas/");
        validator.setReceiver(receiver);
        inOrder = Mockito.inOrder(receiver);
    }

    @Test
    public void testShouldValidate() {
        validator.process(JSON_VALID);
        inOrder.verify(receiver, Mockito.calls(1)).process(JSON_VALID);
    }

    @Test
    public void testShouldInvalidateMissingRequired() {
        validator.process(JSON_INVALID_MISSING_REQUIRED);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldInvalidateUriFormat() {
        validator.process(JSON_INVALID_URI_FORMAT);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldInvalidateDuplicateKey() {
        validator.process(JSON_INVALID_DUPLICATE_KEY);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldInvalidateSyntaxError() {
        validator.process(JSON_INVALID_SYNTAX_ERROR);
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = MetafactureException.class)
    public void testShouldCatchMissingSchemaFile() {
        new JsonValidator("").process("");
    }

    @Test(expected = MetafactureException.class)
    public void testShouldCatchMissingValidOutputFile() {
        validator.setWriteValid("");
        validator.process(JSON_INVALID_MISSING_REQUIRED);
    }

    @Test(expected = MetafactureException.class)
    public void testShouldCatchMissingInvalidOutputFile() {
        validator.setWriteInvalid("");
        validator.process(JSON_INVALID_MISSING_REQUIRED);
    }

    @After
    public void cleanup() {
        validator.closeStream();
    }

}
