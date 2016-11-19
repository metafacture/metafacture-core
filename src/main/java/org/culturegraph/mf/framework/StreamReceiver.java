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
package org.culturegraph.mf.framework;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;

/**
 * Interface implemented by objects which can receive streams.
 * A stream is a sequence of calls to the methods of this interface.
 * The basic structure of the sequence of calls is as follows:
 *
 * STREAM = RECORD*
 * RECORD = startRecord, ENTITY_OR_LITERAL*, endRecord
 * ENTITY_OR_LITERAL = ENTITY | literal
 * ENTITY = startEntity, ENTITY_OR_LITERAL*, endEntity)
 *
 * @see DefaultStreamReceiver
 * @see StreamPipe
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public interface StreamReceiver extends Receiver {

	/**
	 * Send to mark the start of a record. During normal operation each call of
	 * {@code startRecord(String)} is matched by a call of {@link #endRecord()}.
	 * Implementors should, however, expected invocations of
	 * {@code startRecord(String} at any time during processing. In such cases
	 * processing of the current record must be aborted and processing of the new
	 * record started. This behaviour ensures that a receiver can recover from
	 * errors in upstream modules.
	 *
	 * @param identifier identifier of the record. The identifier can be null.
	 */
	void startRecord(String identifier);

	/**
	 * Send to mark the end of a record. Each call of {@code endRecord()} is
	 * matched by a preceding call of {@link #startRecord(String)}.
	 */
	void endRecord();

	/**
	 * Send to mark the start of an entity. This method is only called after
	 * {@link #startRecord(String)} has been called and before
	 * {@link #endRecord()} is called. Each call of {@code startEntity(String)} is
	 * matched by a call of {@link #endEntity()}.
	 *
	 * @param name name of the entity. The name of the entity must never be null.
	 */
	void startEntity(String name);

	/**
	 * Send to mark the end of an entity. Calls to {@code endEntity()} are
	 * always preceded by a call to {@link #startEntity(String)}. This method is
	 * only called after {@link #startRecord(String)} has been called and
	 * before {@link #endRecord()} is called.
	 */
	void endEntity();

	/**
	 * Send to mark a key-value pair in the record. This method is only called
	 * after {@link #startRecord(String)} has been called and before
	 * {@link #endRecord()} is called.
	 *
	 * @param name the key-part of the literal. Must never be null.
	 * @param value the value-part of the literal. Can be null.
	 */
	void literal(String name, String value);

}
