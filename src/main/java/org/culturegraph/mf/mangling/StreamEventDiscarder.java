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

import java.util.EnumSet;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Discards stream events by type. The type of a stream event is either
 * {@linkplain EventType#RECORD record}, {@linkplain EventType#ENTITY entity},
 * {@linkplain EventType#LITERAL literal}, {@linkplain EventType#RESET_STREAM
 * reset-stream} or {@linkplain EventType#CLOSE_STREAM close-stream}. All events
 * which have not been discarded are simply passed on the next module.
 * <p>
 * Use {@link #setDiscardedEvents(EnumSet)} to control which events will be
 * discarded.
 * <p>
 * This module can be used, for example, to extract the contents of a record by
 * discarding the <i>start-record</i> and <i>end-record</i> events and embed the
 * contents of the record in another record.
 *
 * @author Christoph Böhme
 */
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("discard-events")
public class StreamEventDiscarder implements StreamPipe<StreamReceiver> {

	private StreamReceiver receiver;

	private EnumSet<EventType> discardedEvents = EnumSet.noneOf(EventType.class);

	@Override
	public <R extends StreamReceiver> R setReceiver(final R receiver) {
		this.receiver = receiver;
		return receiver;
	}

	/**
	 * Sets whether to discard {@linkplain EventType#RECORD record} events. By
	 * default record events are not discarded.
	 * <p>
	 * This is a convenience method for changing the set of discarded events.
	 * <p>
	 * The state must not be changed while processing a stream. Doing so may
	 * result in unbalanced <i>start-record</i> and <i>end-record</i> events.
	 *
	 * @param discard if true record events will be discarded, otherwise passed
	 *                on.
	 */
	public void setDiscardRecordEvents(final boolean discard) {
		setDiscardEventsByType(EventType.RECORD, discard);
	}

	/**
	 * Sets whether to discard {@linkplain EventType#ENTITY entity} events. By
	 * default entity events are not discarded.
	 * <p>
	 * This is a convenience method for changing the set of discarded events.
	 * <p>
	 * The state must not be changed while processing a stream. Doing so may
	 * result in unbalanced <i>start-entity</i> and <i>end-entity</i> events.
	 *
	 * @param discard if true entity events will be discarded, otherwise passed
	 *                on.
	 */
	public void setDiscardEntityEvents(final boolean discard) {
		setDiscardEventsByType(EventType.ENTITY, discard);
	}

	/**
	 * Sets whether to discard {@linkplain EventType#LITERAL literal} events. By
	 * default literal events are not discarded.
	 * <p>
	 * This is a convenience method for changing the set of discarded events.
	 * <p>
	 * The state must not be changed while processing a stream.
	 *
	 * @param discard if true literal events will be discarded, otherwise passed
	 *                on.
	 */
	public void setDiscardLiteralEvents(final boolean discard) {
		setDiscardEventsByType(EventType.LITERAL, discard);
	}

	/**
	 * Sets whether to discard {@linkplain EventType#RESET_STREAM reset-stream}
	 * and {@linkplain EventType#CLOSE_STREAM close-stream} events. By default
	 * lifecycle events are not discarded.
	 * <p>
	 * This is a convenience method for changing the set of discarded events.
	 * <p>
	 * The state must not be changed while processing a stream.
	 *
	 * @param discard if true the lifecycle events will be discarded, otherwise
	 *                passed on.
	 */
	public void setDiscardLifecycleEvents(final boolean discard) {
		setDiscardEventsByType(EventType.RESET_STREAM, discard);
		setDiscardEventsByType(EventType.CLOSE_STREAM, discard);
	}

	private void setDiscardEventsByType(final EventType type,
	                                    final boolean discard) {
		if (discard) {
			discardedEvents.add(type);
		} else {
			discardedEvents.remove(type);
		}
	}

	/**
	 * Returns the set of currently discarded event types.
	 *
	 * @return a copy of the set of discarded event types. Changes to the returned
	 * set do not affect the module.
	 */
	public EnumSet<EventType> getDiscardedEvents() {
		return EnumSet.copyOf(discardedEvents);
	}

	/**
	 * Sets the stream event types which should be discarded. By default no events
	 * are discarded.
	 * <p>
	 * The set of discarded events must not be changed while processing a stream.
	 * Doing so may result in unbalanced start and end events.
	 *
	 * @param discardedEvents set of event types to discard. The set is copied
	 *                        into an internal representation by the method.
	 *                        Changes to the set do not affect the module.
	 */
	public void setDiscardedEvents(final EnumSet<EventType> discardedEvents) {
		this.discardedEvents = EnumSet.copyOf(discardedEvents);
	}

	@Override
	public void startRecord(final String identifier) {
		if (!discardedEvents.contains(EventType.RECORD)) {
			receiver.startRecord(identifier);
		}
	}

	@Override
	public void endRecord() {
		if (!discardedEvents.contains(EventType.RECORD)) {
			receiver.endRecord();
		}
	}

	@Override
	public void startEntity(final String name) {
		if (!discardedEvents.contains(EventType.ENTITY)) {
			receiver.startEntity(name);
		}
	}

	@Override
	public void endEntity() {
		if (!discardedEvents.contains(EventType.ENTITY)) {
			receiver.endEntity();
		}
	}

	@Override
	public void literal(final String name, final String value) {
		if (!discardedEvents.contains(EventType.LITERAL)) {
			receiver.literal(name, value);
		}
	}

	@Override
	public void resetStream() {
		if (!discardedEvents.contains(EventType.RESET_STREAM)) {
			receiver.resetStream();
		}
	}

	@Override
	public void closeStream() {
		if (!discardedEvents.contains(EventType.CLOSE_STREAM)) {
			receiver.closeStream();
		}
	}

	/**
	 * Types representing stream and lifecycle events.
	 */
	public enum EventType {
		/**
		 * Type representing <i>start-record</i> and <i>end-record</i> stream
		 * events.
		 */
		RECORD,

		/**
		 * Type representing <i>start-entity</i> and <i>end-entity</i> stream
		 * events.
		 */
		ENTITY,

		/**
		 * Type representing the <i>literal</i> stream event.
		 */
		LITERAL,

		/**
		 * Type representing the <i>reset-stream</i> lifecycle event.
		 */
		RESET_STREAM,

		/**
		 * Type representing the <i>close-stream</i> lifecycle event.
		 */
		CLOSE_STREAM
	}

}
