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
package org.culturegraph.mf.strings;

import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * concatenetes all recieved Strings
 *
 * @author markus geipel
 *
 */
public final class StringConcatenator implements ObjectReceiver<String> {

	private StringBuilder builder = new StringBuilder();
	private String separator = "";

	@Override
	public void resetStream() {
		reset();

	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	@Override
	public void closeStream() {
		// nothing to do

	}

	@Override
	public void process(final String obj) {
		builder.append(separator);
		builder.append(obj);

	}

	public void reset(){
		builder = new StringBuilder();
	}

	public String getString(){
		return builder.toString();
	}

}
