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
package org.culturegraph.mf.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.input.BOMInputStream;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;


/**
 * Opens a file and passes a reader for it to the receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Opens a file.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-file")
public final class FileOpener
		extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

	private String encoding = "UTF-8";
	private FileCompression compression = FileCompression.AUTO;

	/**
	 * Returns the encoding used to open the resource.
	 *
	 * @return current default setting
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets the encoding used to open the resource.
	 *
	 * @param encoding
	 *            new encoding
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	public FileCompression getCompression() {
		return compression;
	}

	public void setCompression(final FileCompression compression) {
		this.compression = compression;
	}

	public void setCompression(final String compression) {
		setCompression(FileCompression.valueOf(compression.toUpperCase()));
	}

	@Override
	public void process(final String file) {
		try {
			final InputStream fileStream = new FileInputStream(file);
			try {
				final InputStream decompressor = compression.createDecompressor(fileStream);
				try {

					final Reader reader = new InputStreamReader(new BOMInputStream(
							decompressor), encoding);
					getReceiver().process(reader);
				} catch (final IOException | MetafactureException e) {
					decompressor.close();
					throw e;
				}
			} catch (final IOException | MetafactureException e) {
				fileStream.close();
				throw e;
			}
		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}

}
