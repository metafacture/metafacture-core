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

/**
 * Common functions for object writers.
 *
 * @author Christoph Böhme
 *
 * @param <T>
 * 		object type
 */
public abstract class AbstractObjectWriter<T> implements ConfigurableObjectWriter<T> {

	private String header = DEFAULT_HEADER;
	private String footer = DEFAULT_FOOTER;
	private String separator = DEFAULT_SEPARATOR;

	@Override
	public final String getHeader() {
		return header;
	}

	@Override
	public final void setHeader(final String header) {
		this.header = header;
	}

	@Override
	public final String getFooter() {
		return footer;
	}

	@Override
	public final void setFooter(final String footer) {
		this.footer = footer;
	}

	@Override
	public final String getSeparator() {
		return separator;
	}

	@Override
	public final void setSeparator(final String separator) {
		this.separator = separator;
	}

}
