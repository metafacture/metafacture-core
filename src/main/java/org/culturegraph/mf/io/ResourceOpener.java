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

import java.io.Reader;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;



/**
 * Opens a resource or file and passes a reader for it to the receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Opens a resource.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-resource")
public final class ResourceOpener
		extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

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
	 * @param encoding new encoding
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	@Override
	public void process(final String file) {
		try {
			getReceiver().process(ResourceUtil.getReader(file, encoding));
		} catch (java.io.IOException e) {
			throw new MetafactureException(e);
		}
	}

}
