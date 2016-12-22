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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.annotations.In;

/**
 * @param <T>
 *            object type
 *
 * @author Markus Geipel
 * @author Christoph Böhme
 *
 */
@In(Object.class)
@FluxCommand("write-files")
public final class ObjectFileWriter<T> extends AbstractObjectWriter<T>  {

	private static final String VAR = "${i}";
	private static final Pattern VAR_PATTERN = Pattern.compile(VAR, Pattern.LITERAL);

	private String path;
	private int count;
	private Writer writer;
	private boolean firstObject;
	private boolean closed;

	private String encoding = "UTF-8";
	private FileCompression compression = FileCompression.AUTO;

	public ObjectFileWriter(final String path) {
		super();

		this.path = path;
		startNewFile();

		final Matcher matcher = VAR_PATTERN.matcher(this.path);
		if (!matcher.find()) {
			this.path = this.path + VAR;
		}
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	@Override
	public FileCompression getCompression() {
		return compression;
	}

	@Override
	public void setCompression(final FileCompression compression) {
		this.compression = compression;
	}

	@Override
	public void setCompression(final String compression) {
		setCompression(FileCompression.valueOf(compression.toUpperCase()));
	}

	@Override
	public void process(final T obj) {
		assert !closed;
		try {
			if (firstObject) {
				writer.write(getHeader());
				firstObject = false;
			} else {
				writer.write(getSeparator());
			}
			writer.write(obj.toString());
		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}

	@Override
	public void resetStream() {
		if (!closed) {
			try {
				if (!firstObject) {
					writer.write(getFooter());
				}
				writer.close();
			} catch (final IOException e) {
				throw new MetafactureException(e);
			} finally {
				closed = true;
			}
		}
		startNewFile();
		++count;
	}

	@Override
	public void closeStream() {
		if (!closed) {
			try {
				if (!firstObject) {
					writer.write(getFooter());
				}
				writer.close();
			} catch (final IOException e) {
				throw new MetafactureException(e);
			} finally {
				closed = true;
			}
		}
	}

	private void startNewFile() {
		final Matcher matcher = VAR_PATTERN.matcher(this.path);
		final String path = matcher.replaceAll(String.valueOf(count));
		try {
			final OutputStream file = new FileOutputStream(path);
			try {
				final OutputStream compressor = compression.createCompressor(file, path);
				try {
					writer = new OutputStreamWriter(compressor, encoding);
					firstObject = true;
					closed = false;
				} catch (final IOException e) {
					compressor.close();
					throw e;
				}
			} catch (final IOException e) {
				file.close();
				throw e;
			}
		} catch (final IOException e) {
			throw new MetafactureException("Error creating file '" + path + "'.", e);
		}
	}

}
