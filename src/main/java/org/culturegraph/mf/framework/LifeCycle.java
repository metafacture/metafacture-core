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
package org.culturegraph.mf.framework;

/**
 * Manages the lifecycle of a metastream module. Modules must propagate
 * the events to downstream modules.
 *
 * @author Christoph Böhme
 *
 */
public interface LifeCycle {

	/**
	 * Resets the module to its initial state. All unsaved data is discarded. This
	 * method may throw {@link UnsupportedOperationException} if the model cannot
	 * be reset. This method may be called any time during processing.
	 */
	void resetStream();

	/**
	 * Notifies the module that processing is completed. Resources such as files or
	 * search indexes should be closed. The module cannot be used anymore after
	 * closeStream() has been called. The module may be reset, however, so
	 * it can be used again. This is not guaranteed to work though.
	 * This method may be called any time during processing.
	 */
	void closeStream();

}
