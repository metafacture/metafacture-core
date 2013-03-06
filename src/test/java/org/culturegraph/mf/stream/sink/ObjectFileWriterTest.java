package org.culturegraph.mf.stream.sink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Christoph Böhme
 */
// TODO: Add category for integration tests.
public final class ObjectFileWriterTest {

	private static final String UTF8_ENCODING = "UTF-8";
	private static final String OUTPUT = "Überfacture";
	
	// NO CHECKSTYLE VisibilityModifier FOR 3 LINES:
	// JUnit requires rules to be public
	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();
	
	
	@Test
	public void testOutputIsUTF8Encoded() throws IOException {
		final File file = tempFolder.newFile("test-file");
		final ObjectFileWriter<String> writer = new ObjectFileWriter<String>(file.getAbsolutePath());
		
		writer.process(OUTPUT);
		writer.closeStream();
		
		final byte[] expected= OUTPUT.getBytes(UTF8_ENCODING);
		final InputStream stream = new FileInputStream(file);
		try {
			int i = 0;
			int b;
			while((b = stream.read()) != -1) {
				assertTrue("File contains more bytes than expected", i < expected.length);
				assertEquals("Unexpected file contents", expected[i], b);
				i += 1;
			}
		} finally {
			stream.close();
		}
	}
	
}
