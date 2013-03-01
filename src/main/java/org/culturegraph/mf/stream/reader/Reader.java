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
package org.culturegraph.mf.stream.reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Adds methods 'read' and 'getId' to {@link StreamSender}
 * @author Markus Michael Geipel
 */
public interface Reader extends ObjectPipe<java.io.Reader, StreamReceiver>  {
	
	/**
	 * Reads a single record
	 * @param entry one record
	 */
	void read(final String entry);

}