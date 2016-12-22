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

import java.nio.charset.Charset;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;

/**
 * @param <T> object type
 *
 * @author Christoph Böhme
 *
 */

@Description("Writes objects to stdout")
@In(Object.class)
@FluxCommand("print")
public final class ObjectStdoutWriter<T> extends AbstractObjectWriter<T>  {

	private static final String SET_COMPRESSION_ERROR = "Cannot compress standard out";

	private boolean firstObject = true;
	private boolean closed;

	@Override
	public String getEncoding() {
		return Charset.defaultCharset().toString();
	}

	@Override
	public void setEncoding(final String encoding) {
		throw new UnsupportedOperationException("Cannot change encoding of standard out");
	}

	@Override
	public FileCompression getCompression() {
		return FileCompression.NONE;
	}

	@Override
	public void setCompression(final FileCompression compression) {
		throw new UnsupportedOperationException(SET_COMPRESSION_ERROR);
	}

	@Override
	public void setCompression(final String compression) {
		throw new UnsupportedOperationException(SET_COMPRESSION_ERROR);
	}

	@Override
	public void process(final T obj) {
		assert !closed;

		if (firstObject) {
			System.out.print(getHeader());
			firstObject = false;
		} else {
			System.out.print(getSeparator());
		}
		System.out.print(obj);
	}

	@Override
	public void resetStream() {
		firstObject = true;
	}

	@Override
	public void closeStream() {
		if (!firstObject) {
			System.out.print(getFooter());
		}
		closed = true;
	}

}
