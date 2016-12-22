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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.objects.Triple;


/**
 * @author markus geipel
 *
 */
public final class SortedTripleFileFacade {
	public static final int BUFFERSIZE = 2048;
	private final ObjectInputStream in;
	private final File file;
	private Triple triple;
	private boolean empty;

	public SortedTripleFileFacade(final File file) throws IOException {
		this.file = file;
		in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), BUFFERSIZE));
		next();
	}

	public boolean isEmpty() {
		return empty;
	}

	private void next() throws IOException {
		try {
			triple = Triple.read(in);
			empty = false;

		} catch (EOFException e) {
			empty = true;
			triple = null;
		}
	}

	public void close() {

		try {
			in.close();
		} catch (IOException e) {
			throw new MetafactureException("Error closing input stream", e);
		}
		if (file.exists()) {
			file.delete();
		}
	}

	public Triple peek() {
		if (isEmpty()) {
			return null;
		}
		return triple;
	}

	public Triple pop() throws IOException {
		final Triple triple = peek();
		next();
		return triple;
	}


}
