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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.culturegraph.mf.framework.objects.Triple;

/**
 * @author Christoph Böhme
 *
 */
@FluxCommand("read-triples")
public final class TripleReader extends
		DefaultObjectPipe<String, ObjectReceiver<Triple>> {

	public static final int BUFFERSIZE = 2048;

	@Override
	public void process(final String filename) {
		try {
			final ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename), BUFFERSIZE));

			try {
				while (true) {
					getReceiver().process(Triple.read(in));
				}
			} catch (EOFException e) {
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

}
