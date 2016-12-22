/*
 * Copyright 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.mangling;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.ForwardingStreamPipe;

/**
 * Turns a record into an entity that can be embedded into another record.
 *
 * <p>The module captures the <i>start-record</i> and <i>end-record</i> events
 * and replaces them with <i>start-entity</i> and <i>end-entity</i> events. All
 * other events are forwarded unchanged. The name of the generated entity can be
 * set with {@link #setEntityName(String)}.
 *
 * <p>Optionally, the record identifier can be added to the generated entity.
 * This is configured with {@link #setIdLiteralName(String)}.
 *
 * @author Christoph Böhme
 */
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("record-to-entity")
public class RecordToEntity extends ForwardingStreamPipe {

	/**
	 * Default value for the name of the entity that replaces the record.
	 */
	public static final String DEFAULT_ENTITY_NAME = "record";

	private String entityName = DEFAULT_ENTITY_NAME;
	private String idLiteralName;

	public String getEntityName() {
		return entityName;
	}

	/**
	 * Sets the name of the entity which replaces the record. The default name is
	 * &quot;{@value DEFAULT_ENTITY_NAME}&quot;.
	 *
	 * <p>The entity name may be changed while processing an event stream. It
	 * becomes effective with the next record.
	 *
	 * @param entityName the new name for generated entities. Can be empty which
	 *                   results in unnamed entities being emitted. Must not be
	 *                   null.
	 */
	public void setEntityName(final String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Returns the name of the literal that contains the record id if enabled.
	 *
	 * @return the name of the id literal. If output of id literals is disabled
	 * then null is returned.
	 */
	public String getIdLiteralName() {
		return idLiteralName;
	}

	/**
	 * Enables output of a literal with the record identifier of the converted
	 * record. The literal is emitted directly after the start-entity event for
	 * the record before any other events are generated.
	 *
	 * <p>By default no id literal is generated.
	 *
	 * <p>The id literal may be changed while processing an event stream. The
	 * change becomes effective with the next record.
	 *
	 * @param name the name of the record-id literal
	 */
	public void setIdLiteralName(final String name) {
		idLiteralName = name;
	}

	@Override
	public void startRecord(final String identifier) {
		getReceiver().startEntity(entityName);
		if (idLiteralName != null) {
			getReceiver().literal(idLiteralName, identifier);
		}
	}

	@Override
	public void endRecord() {
		getReceiver().endEntity();
	}

}
