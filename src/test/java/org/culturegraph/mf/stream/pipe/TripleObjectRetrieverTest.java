/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.pipe;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.types.Triple;
import org.culturegraph.mf.types.Triple.ObjectType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link TripleObjectRetriever}.
 * 
 * @author Christoph Böhme
 */
public final class TripleObjectRetrieverTest {

	private static final String SUBJECT = "subject";
	private static final String PREDICATE = "predicate";
	private static final String OBJECT_VALUE = "object-data";
	private static final String ENTITY = "{l=v}";

	private TripleObjectRetriever tripleObjectRetriever;
	
	@Mock
	private ObjectReceiver<Triple> receiver;
	
	// NO CHECKSTYLE VisibilityModifier|DeclarationOrder FOR 3 LINES:
	// JUnit requires rules to be public
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private String objectUrl;
	
	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		
		tripleObjectRetriever = new TripleObjectRetriever();
		tripleObjectRetriever.setReceiver(receiver);
		
		objectUrl = createObjectResource(OBJECT_VALUE);
	}
	
	@After
	public void cleanup() {
		tripleObjectRetriever.closeStream();
	}
	
	@Test
	public void testShouldReplaceObjectValueWithResourceContentRetrievedFromUrl() {
		tripleObjectRetriever.process(new Triple(SUBJECT, PREDICATE, objectUrl));
		
		verify(receiver).process(new Triple(SUBJECT, PREDICATE, OBJECT_VALUE));
	}
	
	@Test
	public void testShouldSkipTriplesWithObjectTypeEntity() {
		tripleObjectRetriever.process(new Triple(SUBJECT, PREDICATE, ENTITY, ObjectType.ENTITY));
		
		verifyZeroInteractions(receiver);
	}
	
	private String createObjectResource(final String contents) throws IOException {
		final File file = tempFolder.newFile();
		
		final Writer writer = new FileWriter(file);
		IOUtils.write(contents, writer);
		writer.close();
		
		return file.toURI().toURL().toString();
	}

}
