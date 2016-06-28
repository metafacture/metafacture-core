/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.stream.sink;

import java.util.function.Consumer;

import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Checks that the received stream events are in the correct order and contain
 * valid data. If not, a user-supplied error handler method is invoked. The
 * error handler can be set via {@link #setErrorHandler(Consumer)}. The default
 * error handler does nothing.
 * <p>
 * After an error occurred and was handled, the module behaves as if the
 * invalid event was never received.
 *
 * @author Christoph Böhme
 *
 */
public final class WellformednessChecker implements StreamReceiver {

	/**
	 * The default error handler which is used if no other error handler was set
	 * via {@link #setErrorHandler(Consumer)}. It does nothing.
	 */
	public static final Consumer<String> DEFAULT_ERROR_HANDLER =
			msg -> { /* do nothing */ };

	private static final String ID_MUST_NOT_BE_NULL = "id must not be null";
	private static final String NAME_MUST_NOT_BE_NULL = "name must not be null";

	private static final String NOT_IN_RECORD = "Not in record";
	private static final String NOT_IN_ENTITY = "Not in entity";
	private static final String IN_ENTITY = "In entity";
	private static final String IN_RECORD = "In record";

	private Consumer<String> errorHandler = DEFAULT_ERROR_HANDLER;

	private int nestingLevel;

	/**
	 * Sets the error handler which is called when a invalid event stream is
	 * received.
	 * <p>
	 * The handler is called with a message describing the error.
	 *
	 * @param errorHandler a method which receives an error message
	 */
	public void setErrorHandler(final Consumer<String> errorHandler) {
		this.errorHandler = errorHandler;
	}

	public Consumer<String> getErrorHandler() {
		return errorHandler;
	}

	@Override
	public void startRecord(final String identifier) {
		if (identifier == null) {
			errorHandler.accept(ID_MUST_NOT_BE_NULL);
		} else if (nestingLevel > 0) {
			errorHandler.accept(IN_RECORD);
		} else {
			nestingLevel += 1;
		}
	}

	@Override
	public void endRecord() {
		if (nestingLevel < 1) {
			errorHandler.accept(NOT_IN_RECORD);
		} else if (nestingLevel > 1) {
			errorHandler.accept(IN_ENTITY);
		} else {
			nestingLevel -= 1;
		}
	}

	@Override
	public void startEntity(final String name) {
		if (name == null) {
			errorHandler.accept(NAME_MUST_NOT_BE_NULL);
		} else if (nestingLevel < 1) {
			errorHandler.accept(NOT_IN_RECORD);
		} else {
			nestingLevel += 1;
		}
	}

	@Override
	public void endEntity() {
		if (nestingLevel < 2) {
			errorHandler.accept(NOT_IN_ENTITY);
		} else {
			nestingLevel -= 1;
		}
	}

	@Override
	public void literal(final String name, final String value) {
		if (name == null) {
			errorHandler.accept(NAME_MUST_NOT_BE_NULL);
		} else if (nestingLevel < 1) {
			errorHandler.accept(NOT_IN_RECORD);
		}
	}

	@Override
	public void resetStream() {
		nestingLevel = 0;
	}

	@Override
	public void closeStream() {
		if (nestingLevel > 0) {
			errorHandler.accept(IN_RECORD);
		}
	}

}
