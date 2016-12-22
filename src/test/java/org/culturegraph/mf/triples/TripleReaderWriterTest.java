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
package org.culturegraph.mf.triples;

import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.objects.Triple;
import org.culturegraph.mf.framework.objects.Triple.ObjectType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for classes {@link TripleReader} and {@link TripleWriter}.
 * TODO: This test class should be split into two separated test classes.
 *
 * @author Christoph Böhme
 *
 */
public final class TripleReaderWriterTest {

	private static final Triple TRIPLE1 = new Triple("S", "P", "O1");
	private static final Triple TRIPLE2 = new Triple("S", "P", "O2", ObjectType.ENTITY);

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Mock
	private ObjectReceiver<Triple> receiver;

	@Test
	public void testShouldWriteAndReadTriples() throws IOException {
		MockitoAnnotations.initMocks(this);

		final File file = tempFolder.newFile();
		final TripleWriter tripleWriter = new TripleWriter(file.getAbsolutePath());
		final TripleReader tripleReader = new TripleReader();
		tripleReader.setReceiver(receiver);

		tripleWriter.process(TRIPLE1);
		tripleWriter.process(TRIPLE2);
		tripleWriter.closeStream();

		tripleReader.process(file.getAbsolutePath());

		verify(receiver).process(TRIPLE1);
		verify(receiver).process(TRIPLE2);
	}

}
