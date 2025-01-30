/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.triples;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.objects.Triple;
import org.metafacture.framework.objects.Triple.ObjectType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Tests for class {@link TripleObjectRetriever}.
 *
 * @author Christoph Böhme
 *
 */
public final class TripleObjectRetrieverTest {

    private static final String SUBJECT = "subject";
    private static final String PREDICATE = "predicate";
    private static final String OBJECT_VALUE = "object-data";
    private static final String ENTITY = "{l=v}";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private ObjectReceiver<Triple> receiver;

    private String objectUrl;
    private TripleObjectRetriever tripleObjectRetriever;

    public TripleObjectRetrieverTest() {
    }

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        tripleObjectRetriever = new TripleObjectRetriever();
        tripleObjectRetriever.setReceiver(receiver);

        objectUrl = createObjectResource(OBJECT_VALUE);
    }

    private String createObjectResource(final String contents) throws IOException {
        final File file = tempFolder.newFile();
        final Writer writer = new FileWriter(file);
        writer.write(contents);
        writer.close();
        return file.toURI().toURL().toString();
    }

    @After
    public void cleanup() {
        tripleObjectRetriever.closeStream();
    }

    @Test
    public void shouldReplaceObjectValueWithResourceContentRetrievedFromUrl() {
        tripleObjectRetriever.process(new Triple(SUBJECT, PREDICATE, objectUrl));

        Mockito.verify(receiver).process(new Triple(SUBJECT, PREDICATE, OBJECT_VALUE));
    }

    @Test
    public void shouldSkipTriplesWithObjectTypeEntity() {
        tripleObjectRetriever.process(
                new Triple(SUBJECT, PREDICATE, ENTITY, ObjectType.ENTITY));

        Mockito.verifyZeroInteractions(receiver);
    }

}
