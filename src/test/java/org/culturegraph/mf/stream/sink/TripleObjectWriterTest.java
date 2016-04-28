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
package org.culturegraph.mf.stream.sink;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.types.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link TripleObjectWriter}.
 *
 * @author Christoph Böhme
 */
public final class TripleObjectWriterTest {

	private static final String SUBJECT1 = "subject1";
	private static final String SUBJECT2 = "subject2";
	private static final String STRUCTURED_SUBJECT_A = "a";
	private static final String STRUCTURED_SUBJECT_B = "b";
	private static final String STRUCTURED_SUBJECT = STRUCTURED_SUBJECT_A + "/" + STRUCTURED_SUBJECT_B;
	private static final String PREDICATE = "predicate";
	private static final String OBJECT1 = "object-data 1";
	private static final String OBJECT2 = "object-data 2";

	private TripleObjectWriter tripleObjectWriter;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private String baseDir;

	@Before
	public void setup() throws IOException {
		baseDir = tempFolder.newFolder().getAbsolutePath();
		tripleObjectWriter = new TripleObjectWriter(baseDir);
	}

	@After
	public void cleanup() {
		tripleObjectWriter.closeStream();
	}

	@Test
	public void testShouldWriteObjectOfTripleIntoFile() throws IOException {
		tripleObjectWriter.process(new Triple(SUBJECT1, PREDICATE, OBJECT1));
		tripleObjectWriter.process(new Triple(SUBJECT2, PREDICATE, OBJECT2));

		final String filename1 = baseDir + File.separator + SUBJECT1 + File.separator + PREDICATE;
		final String filename2 = baseDir + File.separator + SUBJECT2 + File.separator + PREDICATE;
		assertEquals(get(filename1), OBJECT1);
		assertEquals(get(filename2), OBJECT2);
	}

	@Test
	public void testShouldMapStructuredSubjectsToDirectories() throws IOException {
		tripleObjectWriter.process(new Triple(STRUCTURED_SUBJECT, PREDICATE, OBJECT1));

		final String filename = baseDir
				+ File.separator + STRUCTURED_SUBJECT_A + File.separator + STRUCTURED_SUBJECT_B
				+ File.separator + PREDICATE;
		assertEquals(get(filename), OBJECT1);
	}

	private String get(final String filename) throws IOException {
		final InputStream stream = new FileInputStream(filename);
		return IOUtils.toString(stream, tripleObjectWriter.getEncoding());
	}

}
