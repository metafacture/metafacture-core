/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * Opens a gz file and passes a reader for it to the receiver.
 *
 * @deprecated Use FileOpener instead and set compression to AUTO or bzip2

 * @author Markus Geipel
 *
 */
@Description("Opens a gz file.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-gzip")
@Deprecated
public final class GzipOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> implements Opener {

	private static final int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private String encoding = "UTF-8";

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

	/**
	 * @param bufferSize
	 *            in MB
	 */
	public void setBufferSize(final int bufferSize) {
		this.bufferSize = bufferSize * 1024 * 1024;
	}

	@Override
	public void process(final String file) {
		try {
			getReceiver().process(
					new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), encoding),
							bufferSize));
		} catch (FileNotFoundException e) {
			throw new MetafactureException(e);
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}
}
