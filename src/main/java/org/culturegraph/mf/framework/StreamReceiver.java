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

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;
import org.culturegraph.mf.stream.sink.WellformednessChecker;

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
 * The {@link WellformednessChecker} can be used to check if a stream conforms
 * to these rules.
 *
 * @see DefaultStreamReceiver
 * @see StreamPipe
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
public interface StreamReceiver extends Receiver {

	/**
	 * Sent to mark the start of a record.
	 * Each call of {@code startRecord()} is matched by a call of {@code endRecord()}.
	 *
	 * @param identifier identifier of the record. The identifier can be null
	 */
	void startRecord(String identifier);

	/**
	 * Sent to mark the end of a record.
	 * Calls to {@code endRecord()} are always preceded by a call to {@code startRecord()}.
	 */
	void endRecord();

	/**
	 * Sent to mark the start of an entity.
	 * This method is only called after {@code startRecord()} has been called and
	 * before {@code endRecord()} is called. Each call of {@code startEntity()} is
	 * matched by a call of {@code endEntity()}.
	 *
	 * @param name name of the entity. The name of the entity should never be null
	 */
	void startEntity(String name);

	/**
	 * Sent to mark the end of an entity.
	 * Calls to {@code endEntity()} are always preceded by a call to {@code startEntity()}.
	 * This method is only called after {@code startRecord()} has been called and
	 * before {@code endRecord()} is called.
	 */
	void endEntity();

	/**
	 * Sent to mark a key-value pair in the record.
	 * This method is only called after {@code startRecord()} has been called and
	 * before {@code endRecord()} is called.
	 *
	 * @param name the key-part of the literal. Should never be null
	 * @param value the value-part of the literal. Can be null
	 */
	void literal(String name, String value);

}
