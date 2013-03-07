/**
 * 
 */
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;


/**
 * @param <T>
 *            object type
 * 
 * @author Markus Geipel
 * 
 */

public final class ObjectFileWriter<T> implements ObjectReceiver<T> {

	private static final String VAR = "${i}";
	private static final Pattern VAR_PATTERN = Pattern.compile(VAR, Pattern.LITERAL);

	private String path;
	private int count;
	private Writer writer;
	
	private String encoding = "UTF-8";

	public ObjectFileWriter(final String path) {
		this.path = path;
		startNewFile();

		final Matcher matcher = VAR_PATTERN.matcher(this.path);
		if (!matcher.find()) {
			this.path = this.path + VAR;
		}
	}
	
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
	
	private void startNewFile() {
		final Matcher matcher = VAR_PATTERN.matcher(this.path);
		final String path = matcher.replaceAll(String.valueOf(count));
		try {
			writer = new OutputStreamWriter(new FileOutputStream(path), encoding);
		} catch (IOException e) {
			throw new MetafactureException("Error creating file '" + path + "'.", e);
		}
	}

	@Override
	public void process(final T obj) {
		try {
			writer.write(obj.toString());
			writer.append('\n');
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

	@Override
	public void resetStream() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
		startNewFile();
		++count;
	}

	@Override
	public void closeStream() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

}
