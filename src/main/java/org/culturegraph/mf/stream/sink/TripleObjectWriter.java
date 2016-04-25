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
package org.culturegraph.mf.stream.sink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.Triple;

/**
 * Writes the object value of the triple into a file. The filename
 * is constructed from subject and predicate.
 *
 * Please note: This module does not check if the filename constructed
 * from subject and predicate stays within {@code baseDir}. THIS MODULE
 * SHOULD NOT BE USED IN ENVIRONMENTS IN WHICH THE VALUES OF SUBJECT AND
 * PREDICATE A PROVIDED BY AN UNTRUSTED SOURCE!
 *
 * @author Christoph Böhme
 */
@Description("Writes the object value of the triple into a file. The filename is "
		+ "constructed from subject and predicate. Please note: This module does "
		+ "not check if the filename constructed from subject and predicate stays "
		+ "within `baseDir`. THIS MODULE SHOULD NOT BE USED IN ENVIRONMENTS IN WHICH "
		+ "THE VALUES OF SUBJECT AND PREDICATE A PROVIDED BY AN UNTRUSTED SOURCE!")
@In(Triple.class)
@Out(Void.class)
@FluxCommand("write-triple-objects")
public final class TripleObjectWriter extends DefaultObjectReceiver<Triple> {

	private final String baseDir;

	private String encoding = "UTF-8";

	public TripleObjectWriter(final String baseDir) {
		this.baseDir = baseDir;
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

	@Override
	public void process(final Triple triple) {
		final String file = FilenameUtils.concat(
				FilenameUtils.concat(baseDir, triple.getSubject()), triple.getPredicate());

		ensurePathExists(file);

		try {
			final Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
			IOUtils.write(triple.getObject(), writer);
			writer.close();
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

	private void ensurePathExists(final String path) {
		final File parent = new File(path).getAbsoluteFile().getParentFile();
		parent.mkdirs();
	}

}
