/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.sink;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectReceiver;
import org.culturegraph.mf.types.Triple;

/**
 * @author Christoph Böhme
 *
 */
public final class TripleWriter extends DefaultObjectReceiver<Triple> {

	public static final int BUFFERSIZE = 2048;

	private final String filename;
	
	private ObjectOutputStream outputStream;
	
	public TripleWriter(final String filename) {
		this.filename = filename;
		resetStream();
	}
	
	@Override
	public void process(final Triple obj) {
		try {
			obj.write(outputStream);
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}
	
	@Override
	public void resetStream() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename), BUFFERSIZE));
		} catch (IOException e) {
			throw new MetafactureException(e);
		} 
	}
	
	@Override
	public void closeStream() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException e) {
			throw new MetafactureException(e);
		} 
	}
	
}
