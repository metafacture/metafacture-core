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

import java.io.IOException;
import java.io.Writer;

import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * @param <T>
 *            object type
 *
 * @author Christoph Böhme, Markus Geipel
 *
 */
public final class ObjectJavaIoWriter<T> implements ObjectReceiver<T> {

	private Writer writer;
	private boolean closed;
	private final IoWriterFactory writerFactory;

	public ObjectJavaIoWriter(final Writer writer) {
		this.writer = writer;
		writerFactory = null;
	}

	public ObjectJavaIoWriter(final IoWriterFactory writerFactory) {
		this.writerFactory = writerFactory;
		this.writer = writerFactory.createWriter();
	}

	@Override
	public void process(final T obj) {
		assert !closed;
		try {
			writer.write(obj.toString());
			writer.append('\n');
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

	@Override
	public void resetStream() {
		if(writerFactory==null){
			throw new UnsupportedOperationException("Cannot reset ObjectJavaIoWriter. No IOWriterFactory set.");
		}
		writer = writerFactory.createWriter();

	}

	@Override
	public void closeStream() {
		if (!closed) {
			try {
				writer.close();
			} catch (IOException e) {
				throw new MetafactureException(e);
			}finally{
				closed=true;
			}

		}
	}
}
