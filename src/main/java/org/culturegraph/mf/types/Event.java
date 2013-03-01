/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.types;

/**
 * Data type for stream events. Used by {@sink.EventList}.
 * 
 * @author Christoph Böhme <c.boehme@dnb.de>
 */
public final class Event {
	/**
	 * Event types
	 */
	public enum Type {
		START_RECORD, END_RECORD, START_ENTITY, END_ENTITY, LITERAL
	}
	
	private final Type type;
	private final String name;
	private final String value;
	
	public Event(final Type type) {
		this(type, null);
	}
	
	public Event(final Type type, final String name) {
		this(type, name, null);
	}
	
	public Event(final Type type, final String name, final String value) {
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
