/*
 * Copyright 2016 Christoph Böhme
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

import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * Back end implementations for {@link ObjectWriter} should offer
 * a default set of configuration options. These are defined by
 * this interface.
 *
 * @param <T> object type
 * @author Christoph Böhme
 */
public interface ConfigurableObjectWriter<T> extends ObjectReceiver<T> {

	String DEFAULT_HEADER = "";
	String DEFAULT_FOOTER = "\n";
	String DEFAULT_SEPARATOR = "\n";

	/**
	 * Returns the encoding used by the underlying writer.
	 *
	 * @return current encoding
	 */
	String getEncoding();

	/**
	 * Sets the encoding used by the underlying writer.
	 *
	 * @param encoding
	 *            name of the encoding
	 */
	void setEncoding(String encoding);

	/**
	 * Returns the compression mode.
	 *
	 * @return current compression mode
	 */
	FileCompression getCompression();

	/**
	 * Sets the compression mode.
	 *
	 * @param compression type of compression
	 */
	void setCompression(final FileCompression compression);

	/**
	 * Sets the compression mode.
	 *
	 * @param compression type of compression
	 */
	void setCompression(final String compression);

	/**
	 * Returns the header which is output before the first object.
	 *
	 * @return header string
	 */
	String getHeader();

	/**
	 * Sets the header which is output before the first object.
	 *
	 * @param header new header string
	 */
	void setHeader(final String header);

	/**
	 * Returns the footer which is output after the last object.
	 *
	 * @return footer string
	 */
	String getFooter();

	/**
	 * Sets the footer which is output after the last object.
	 *
	 * @param footer new footer string
	 */
	void setFooter(final String footer);

	/**
	 * Returns the separator which is output between objects.
	 *
	 * @return separator string
	 */
	String getSeparator();

	/**
	 * Sets the separator which is output between objects.
	 *
	 * @param separator new separator string
	 */
	void setSeparator(final String separator);

}
