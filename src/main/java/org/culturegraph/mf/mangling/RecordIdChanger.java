/*
 * Copyright 2016 Christoph Böhme
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

import org.culturegraph.mf.flowcontrol.StreamBuffer;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StandardEventNames;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Replaces the record id with the value of a literal from the record. The name
 * of the literal can be configured using {@link #setIdLiteral(String)}.
 * <p>
 * If a record contains multiple matching literals, the value of the last
 * literal is used as id.
 * <p>
 * This module can optionally remove records which do not have an id literal
 * from the stream. This is configured through the
 * {@link #setKeepRecordsWithoutIdLiteral(boolean)} parameter.
 * <p>
 * The example shows how this module operates in its default configuration:
 * Given the following sequence of events:
 * <pre>{@literal
 * start-record "old-id"
 * start-entity "author"
 * literal "name": Kurt
 * end-entity
 * literal "_id": new-id-1
 * literal "_id": new-id-2
 * end-record
 * }</pre>
 *
 * the {@code RecordIdChanger} emits the following event stream:
 * <pre>{@literal
 * start-record "new-id-2"
 * start-entity "author"
 * literal "name": Kurt
 * end-entity
 * end-record
 * }</pre>
 *
 * By default, the id literals are removed from the record. This can be changed
 * through {@link #setKeepIdLiteral(boolean)}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
@Description("By default changes the record id to the value of the '_id' literal (if present). Use the contructor to choose another literal as id source.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("change-id")
public final class RecordIdChanger extends DefaultStreamPipe<StreamReceiver> {

	private final StreamBuffer streamBuffer = new StreamBuffer();
	private final EntityPathTracker entityPathTracker = new EntityPathTracker();

	private String idLiteral = StandardEventNames.ID;
	private String currentIdentifier;
	private String originalIdentifier;
	private boolean keepRecordsWithoutIdLiteral = true;
	private boolean keepIdLiteral;

	/**
	 * Sets the name of the literal that contains the new record id. This must be
	 * a qualified literal name including the entities in which the literal is
	 * contained.
	 * <p>
	 * For instance, the id literal &ldquo;metadata.id&rdquo; matches only
	 * literals named &ldquo;id&rdquo; which are part of an entity named
	 * &ldquo;metadata&rdquo;.
	 * <p>
	 * The default value is &ldquo;{@value StandardEventNames#ID}&rdquo;.
	 * <p>
	 * This parameter must only be changed between records otherwise the
	 * behaviour of the module is undefined.
	 *
	 * @param idLiteral a qualified literal name
	 */
	public void setIdLiteral(final String idLiteral) {
		this.idLiteral = idLiteral;
	}

	public String getIdLiteral() {
		return idLiteral;
	}

	/**
	 * Controls whether records without an id literal are kept in the stream or
	 * removed from the stream.
	 * <p>
	 * By default records without an id literal are kept in the stream.
	 * <p>
	 * This parameter may be changed at any time it becomes effective with the
	 * next <i>end-record</i> event.
	 *
	 * @param keepRecordsWithoutIdLiteral true to keep records without id
	 * literal, false to remove them
	 */
	public void setKeepRecordsWithoutIdLiteral(
			final boolean keepRecordsWithoutIdLiteral) {
		this.keepRecordsWithoutIdLiteral = keepRecordsWithoutIdLiteral;
	}

	public boolean getKeepRecordsWithoutIdLiteral() {
		return keepRecordsWithoutIdLiteral;
	}

	/**
	 * Controls whether the id literal is kept in the record after changing the
	 * record id. If a record contains multiple id literals, all of them are
	 * removed.
	 * <p>
	 * By default the id literal is removed from the stream.
	 * <p>
	 * This parameter must only be changed between records otherwise the
	 * behaviour of the module is undefined.
	 *
	 * @param keepIdLiteral true to keep id literals in records, false to
	 * remove them
	 */
	public void setKeepIdLiteral(final boolean keepIdLiteral) {
		this.keepIdLiteral = keepIdLiteral;
	}

	public boolean getKeepIdLiteral() {
		return keepIdLiteral;
	}

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		currentIdentifier = null;
		originalIdentifier = identifier;
		entityPathTracker.startRecord(identifier);
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		if (currentIdentifier != null || keepRecordsWithoutIdLiteral) {
			if (currentIdentifier == null) {
				getReceiver().startRecord(originalIdentifier);
			} else {
				getReceiver().startRecord(currentIdentifier);
			}
			streamBuffer.replay();
			getReceiver().endRecord();
		}
		streamBuffer.clear();
		entityPathTracker.endRecord();
	}

	@Override
	public void startEntity(final String name) {
		streamBuffer.startEntity(name);
		entityPathTracker.startEntity(name);
	}

	@Override
	public void endEntity() {
		streamBuffer.endEntity();
		entityPathTracker.endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		final String qualifiedName = entityPathTracker.getCurrentPathWith(name);
		if (idLiteral.equals(qualifiedName)) {
			currentIdentifier = value;
			if (!keepIdLiteral) {
				return;
			}
		}
		streamBuffer.literal(name, value);
	}

	@Override
	public void onSetReceiver() {
		streamBuffer.setReceiver(getReceiver());
	}

	@Override
	public void onResetStream() {
		streamBuffer.clear();
		entityPathTracker.resetStream();
	}

	@Override
	public void onCloseStream() {
		streamBuffer.clear();
		entityPathTracker.closeStream();
	}

}
