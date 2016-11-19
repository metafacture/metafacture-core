/* Copyright 2013 hbz, Pascal Christoph.
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

package org.culturegraph.mf.stream.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Opens (aka 'untar') a tar archive and passes every entry .
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("Opens a tar archive and passes every entry.")
@In(Reader.class)
@Out(Reader.class)
@FluxCommand("open-tar")
public class TarReader extends DefaultObjectPipe<Reader, ObjectReceiver<Reader>> {
	@Override
	public void process(final Reader reader) {
		TarArchiveInputStream tarInputStream = null;
		try {
			tarInputStream = new TarArchiveInputStream(new ReaderInputStream(reader));
			TarArchiveEntry entry = null;
			while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					final byte[] buffer = new byte[(int) entry.getSize()];
					while ((tarInputStream.read(buffer)) > 0) {
						getReceiver().process(new StringReader(new String(buffer)));
					}
				}
			}
			tarInputStream.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(tarInputStream);
		}
	}
}
