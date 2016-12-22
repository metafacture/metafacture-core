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
package org.culturegraph.mf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.input.ReaderInputStream;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Opens (aka 'untar') a tar archive and passes every entry.
 *
 * @author Pascal Christoph (dr0i)
 * @author Christoph Böhme
 */
@Description("Opens a tar archive and passes every entry.")
@In(Reader.class)
@Out(Reader.class)
@FluxCommand("open-tar")
public class TarReader
		extends DefaultObjectPipe<Reader, ObjectReceiver<Reader>> {

	@Override
	public void process(final Reader reader) {
		try (
				InputStream stream = new ReaderInputStream(reader,
						Charset.defaultCharset());
				ArchiveInputStream tarStream = new TarArchiveInputStream(stream);
		) {
			ArchiveEntry entry;
			while ((entry = tarStream.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					processFileEntry(tarStream);
				}
			}
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

	private void processFileEntry(ArchiveInputStream archiveStream)
			throws IOException {
		try (
				InputStream entryStream = new ArchiveEntryInputStream(archiveStream);
				Reader entryReader = new InputStreamReader(entryStream);
		) {
			getReceiver().process(entryReader);
		}
	}

	/**
	 * Wraps an {@link ArchiveInputStream} so that individual archive entries
	 * can be read by code working on arbitrary {@link InputStream}s.
	 * <p>
	 * This is required as the semantics of {@code ArchiveInputStream} differ
	 * from standard {@code InputStream} behaviour. Archive streams must only be
	 * closed after reading all entries and not after reading a single entry.
	 * Because of this, it is not possible to pass an {@code ArchiveInputStream}
	 * to code expecting an {@code InputStream} as usually the received stream
	 * will be closed after the code is finished with it. In case of an {@code
	 * ArchiveInputStream} this would not close the stream for the current entry
	 * but the complete archive stream which might not be expected by the caller .
	 * <p>
	 * This class hides this difference by ignoring calls of the
	 * {@link #close()} method.
	 *
	 * @author Christoph Böhme
	 */
	private static class ArchiveEntryInputStream extends InputStream {

		private final ArchiveInputStream archiveStream;

		ArchiveEntryInputStream(ArchiveInputStream archiveStream) {
			this.archiveStream = archiveStream;
		}

		@Override
		public int read() throws IOException {
			return archiveStream.read();
		}

		@Override
		public int read(byte[] b) throws IOException {
			return archiveStream.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return archiveStream.read(b, off, len);
		}

		@Override
		public long skip(long n) throws IOException {
			return archiveStream.skip(n);
		}

		@Override
		public int available() throws IOException {
			return archiveStream.available();
		}

		@Override
		public void close() throws IOException {
			// Do not delegate calls of close() to the archive stream
		}

		@Override
		public void mark(int readlimit) {
			archiveStream.mark(readlimit);
		}

		@Override
		public void reset() throws IOException {
			archiveStream.reset();
		}

		@Override
		public boolean markSupported() {
			return archiveStream.markSupported();
		}

	}

}
