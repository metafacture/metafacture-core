package org.metafacture.io;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ByteStreamFileWriterTest {

    private ByteStreamFileWriter byteStreamFileWriter;

    @Before
    public void setupSystemUnderTest() {

        byteStreamFileWriter = new ByteStreamFileWriter();
    }

    @Test
    public void shouldWriteBytesToFile() throws IOException {

        File outputFile = tempFolder.newFile();
        byteStreamFileWriter.setFileNameSupplier(() -> outputFile);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        assertThat(outputFile).isFile();
        assertThat(contentOf(outputFile)).isEqualTo(SOME_DATA);
    }

    @Test
    public void shouldStartNewOutputFileOnResetStreamEvent() throws IOException {

        List<File> outputFiles = asList(
                tempFolder.newFile(),
                tempFolder.newFile());
        byteStreamFileWriter.setFileNameSupplier(outputFiles.iterator()::next);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.resetStream();
        byteStreamFileWriter.process(SOME_MORE_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        assertThat(outputFiles.get(0)).isFile();
        assertThat(contentOf(outputFiles.get(0))).isEqualTo(SOME_DATA);
        assertThat(outputFiles.get(1)).isFile();
        assertThat(contentOf(outputFiles.get(1))).isEqualTo(SOME_MORE_DATA);
    }

    @Test
    public void shouldOverwriteExistingFilesByDefault() throws IOException {

        File outputFile = tempFolder.newFile();
        byteStreamFileWriter.setFileNameSupplier(() -> outputFile);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.resetStream();
        byteStreamFileWriter.process(SOME_MORE_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        assertThat(outputFile).isFile();
        assertThat(contentOf(outputFile)).isEqualTo(SOME_MORE_DATA);
    }

    @Test
    public void shouldAppendIfParameterAppendIfFileExistsIsSet() throws IOException {


        File outputFile = tempFolder.newFile();
        byteStreamFileWriter.setFileNameSupplier(() -> outputFile);
        byteStreamFileWriter.setAppendIfFileExists(true);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.resetStream();
        byteStreamFileWriter.process(SOME_MORE_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        assertThat(outputFile).isFile();
        assertThat(contentOf(outputFile)).isEqualTo(SOME_DATA + SOME_MORE_DATA);
    }

    private static final String SOME_DATA = "Nil desperandum";
    private static final String SOME_MORE_DATA = "De omnibus dubitandum";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

}
