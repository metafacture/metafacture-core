package org.metafacture.io;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ByteStreamFileWriterTest {

    private static final String SOME_DATA = "Nil desperandum";
    private static final String SOME_MORE_DATA = "De omnibus dubitandum";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private ByteStreamFileWriter byteStreamFileWriter;

    public ByteStreamFileWriterTest() {
    }

    @Before
    public void setupSystemUnderTest() {
        byteStreamFileWriter = new ByteStreamFileWriter();
    }

    @Test
    public void shouldWriteBytesToFile() throws IOException {
        final File outputFile = tempFolder.newFile();
        byteStreamFileWriter.setFileNameSupplier(() -> outputFile);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        Assertions.assertThat(outputFile).isFile();
        Assertions.assertThat(Assertions.contentOf(outputFile)).isEqualTo(SOME_DATA);
    }

    @Test
    public void shouldStartNewOutputFileOnResetStreamEvent() throws IOException {
        final List<File> outputFiles = Arrays.asList(
                tempFolder.newFile(),
                tempFolder.newFile());
        byteStreamFileWriter.setFileNameSupplier(outputFiles.iterator()::next);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.resetStream();
        byteStreamFileWriter.process(SOME_MORE_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        Assertions.assertThat(outputFiles.get(0)).isFile();
        Assertions.assertThat(Assertions.contentOf(outputFiles.get(0))).isEqualTo(SOME_DATA);
        Assertions.assertThat(outputFiles.get(1)).isFile();
        Assertions.assertThat(Assertions.contentOf(outputFiles.get(1))).isEqualTo(SOME_MORE_DATA);
    }

    @Test
    public void shouldOverwriteExistingFilesByDefault() throws IOException {
        final File outputFile = tempFolder.newFile();
        byteStreamFileWriter.setFileNameSupplier(() -> outputFile);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.resetStream();
        byteStreamFileWriter.process(SOME_MORE_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        Assertions.assertThat(outputFile).isFile();
        Assertions.assertThat(Assertions.contentOf(outputFile)).isEqualTo(SOME_MORE_DATA);
    }

    @Test
    public void shouldAppendIfParameterAppendIfFileExistsIsSet() throws IOException {
        final File outputFile = tempFolder.newFile();
        byteStreamFileWriter.setFileNameSupplier(() -> outputFile);
        byteStreamFileWriter.setAppendIfFileExists(true);

        byteStreamFileWriter.process(SOME_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.resetStream();
        byteStreamFileWriter.process(SOME_MORE_DATA.getBytes(StandardCharsets.UTF_8));
        byteStreamFileWriter.closeStream();

        Assertions.assertThat(outputFile).isFile();
        Assertions.assertThat(Assertions.contentOf(outputFile)).isEqualTo(SOME_DATA + SOME_MORE_DATA);
    }

}
