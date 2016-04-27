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
 * Default implementation for {@link StreamPipe}s which simply
 * does nothing.
 *
 * @param <R> receiver type of the downstream receiver
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
public class DefaultStreamPipe<R extends Receiver>
		extends DefaultSender<R> implements StreamPipe<R> {

	// CHECKSTYLE OFF: StrictDuplicateCode
	// Code duplication in DefaultStreamPipe and DefaultStreamReceiver
	// cannot be avoided. DefaultStreamPipe combines the logic
	// from DefaultSender and DefaultStreamReceiver but can only
	// have one of these classes as its base class. Hence, the
	// logic from the second one must be duplicated here.

	@Override
	public void startRecord(final String identifier) {
		// Default implementation does nothing
	}

	@Override
	public void endRecord() {
		// Default implementation does nothing
	}

	@Override
	public void startEntity(final String name) {
		// Default implementation does nothing
	}

	@Override
	public void endEntity() {
		// Default implementation does nothing
	}

	@Override
	public void literal(final String name, final String value) {
		// Default implementation does nothing
	}

	// CHECKSTYLE ON: StrictDuplicateCode

}
