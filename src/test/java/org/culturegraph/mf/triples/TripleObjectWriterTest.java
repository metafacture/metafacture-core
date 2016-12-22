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
package org.culturegraph.mf.triples;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.culturegraph.mf.framework.objects.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for class {@link TripleObjectWriter}.
 *
 * @author Christoph Böhme
 */
public final class TripleObjectWriterTest {

	private static final String SUBJECT1 = "subject1";
	private static final String SUBJECT2 = "subject2";
	private static final String STRUCTURED_SUBJECT_A = "a";
	private static final String STRUCTURED_SUBJECT_B = "b";
	private static final String STRUCTURED_SUBJECT = STRUCTURED_SUBJECT_A + "/" +
			STRUCTURED_SUBJECT_B;
	private static final String PREDICATE = "predicate";
	private static final String OBJECT1 = "object-data 1";
	private static final String OBJECT2 = "object-data 2";

	private TripleObjectWriter tripleObjectWriter;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private Path baseDir;

	@Before
	public void createSystemUnderTest() throws IOException {
		baseDir = tempFolder.newFolder().toPath();
		tripleObjectWriter = new TripleObjectWriter(
		    baseDir.toAbsolutePath().toString());
	}

	@After
	public void cleanup() {
		tripleObjectWriter.closeStream();
	}

	@Test
	public void shouldWriteObjectOfTripleIntoFile() throws IOException {
		tripleObjectWriter.process(new Triple(SUBJECT1, PREDICATE, OBJECT1));
		tripleObjectWriter.process(new Triple(SUBJECT2, PREDICATE, OBJECT2));

		final Path file1 = baseDir.resolve(Paths.get(SUBJECT1, PREDICATE));
		final Path file2 = baseDir.resolve(Paths.get(SUBJECT2, PREDICATE));
		assertEquals(OBJECT1, readFileContents(file1));
		assertEquals(OBJECT2, readFileContents(file2));
	}

	@Test
	public void shouldMapStructuredSubjectsToDirectories() throws IOException {
		tripleObjectWriter.process(new Triple(STRUCTURED_SUBJECT, PREDICATE, OBJECT1));

    final Path file = baseDir.resolve(Paths.get(STRUCTURED_SUBJECT_A,
        STRUCTURED_SUBJECT_B, PREDICATE));
		assertEquals(readFileContents(file), OBJECT1);
	}

	private String readFileContents(final Path file) throws IOException {
    final byte[] data = Files.readAllBytes(file);
    return new String(data, tripleObjectWriter.getEncoding());
	}

}
