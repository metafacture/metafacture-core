/*
 * Copyright 2013, 2014, 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.test.reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Interface for module implementations that can be instanciated by the
 * {@link MultiFormatReader}.
 *
 * @author Markus Michael Geipel
 */
public interface Reader extends ObjectPipe<java.io.Reader, StreamReceiver>  {

	// Marker interface

}
