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
package org.culturegraph.mf.javaintegration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Stores the received stream events in a list.
 *
 * @author Christoph Böhme
 *
 */
public final class EventList implements StreamReceiver {

	private final List<Event> events = new ArrayList<Event>();

	private boolean closed;

	public List<Event> getEvents() {
		return Collections.unmodifiableList(events);
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void startRecord(final String identifier) {
		assert !closed;
		events.add(new Event(Event.Type.START_RECORD, identifier));
	}

	@Override
	public void endRecord() {
		assert !closed;
		events.add(new Event(Event.Type.END_RECORD));
	}

	@Override
	public void startEntity(final String name) {
		assert !closed;
		events.add(new Event(Event.Type.START_ENTITY, name));
	}

	@Override
	public void endEntity() {
		assert !closed;
		events.add(new Event(Event.Type.END_ENTITY));
	}

	@Override
	public void literal(final String name, final String value) {
		assert !closed;
		events.add(new Event(Event.Type.LITERAL, name, value));
	}

	@Override
	public void resetStream() {
		closed = false;
		events.clear();
	}

	@Override
	public void closeStream() {
		closed = true;
	}

	/**
	 * Data type for stream events.
	 *
	 * @author Christoph Böhme
	 */
	public static final class Event {

		/**
		 * Event types
		 */
		public enum Type {
			START_RECORD, END_RECORD, START_ENTITY, END_ENTITY, LITERAL
		}

		private final Type type;
		private final String name;
		private final String value;

		Event(final Type type) {
			this(type, null);
		}

		Event(final Type type, final String name) {
			this(type, name, null);
		}

		Event(final Type type, final String name, final String value) {
			this.type = type;
			this.name = name;
			this.value = value;
		}

		public Type getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append(type);
			if (name != null) {
				builder.append("(" );
				builder.append(name);
				if (value != null) {
					builder.append("=");
					builder.append(value);
				}
				builder.append(")");
			}
			return builder.toString();
		}

	}

}
