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

import java.util.ArrayDeque;
import java.util.Deque;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;

/**
 * Tracks the <i>path</i> of the current entity. The entity path consists of the
 * names of all parent entities of the current entity separated by a separator
 * string. For example, the following sequence of events yields the path
 * <i>granny.mommy.me</i>:
 * <pre>{@literal
 * start-record "1"
 * start-entity "granny"
 * start-entity "mommy"
 * start-entity "me"
 * }</pre>
 *
 * <p>The current path is returned from {@link #getCurrentPath()}.
 *
 * @author Christoph Böhme
 * @see StreamFlattener
 */
public class EntityPathTracker extends DefaultStreamReceiver {

	public static final String DEFAULT_ENTITY_SEPARATOR = ".";

	private final Deque<String> entityStack = new ArrayDeque<String>();
	private final StringBuilder currentPath = new StringBuilder();

	private String entitySeparator = DEFAULT_ENTITY_SEPARATOR;

	/**
	 * Returns the current entity path.
	 *
	 * @return the current entity path or an empty string if not within a record.
	 */
	public String getCurrentPath() {
		return currentPath.toString();
	}

	/**
	 * Returns the current entity path with the given literal name appended.
	 *
	 * @param literalName the literal name to append to the current entity path.
	 *                    Must not be null.
	 * @return the current entity path with the literal name appended. The {@link
	 * #getEntitySeparator()} is used to separate both unless no entity was
	 * received yet in which case only the literal name is returned.
	 */
	public String getCurrentPathWith(final String literalName) {
		if (entityStack.size() == 0) {
			return literalName;
		}
		return getCurrentPath() + entitySeparator + literalName;
	}

	/**
	 * Returns the name of the current entity.
	 *
	 * @return the name of the current entity or null if not in an entity.
	 */
	public String getCurrentEntityName() {
		return entityStack.peek();
	}

	public String getEntitySeparator() {
		return entitySeparator;
	}

	/**
	 * Sets the separator between entity names in the path. The default separator
	 * is &quot;{@value DEFAULT_ENTITY_SEPARATOR}&quot;.
	 *
	 * <p>The separator must not be changed while processing a stream.
	 *
	 * @param entitySeparator the new entity separator. Can be empty to join
	 *                        entity names without any separator. Multi-character
	 *                        separators are also supported. Must not be null.
	 */
	public void setEntitySeparator(final String entitySeparator) {
		this.entitySeparator = entitySeparator;
	}

	@Override
	public void startRecord(final String identifier) {
		clearStackAndPath();
	}

	@Override
	public void endRecord() {
		clearStackAndPath();
	}

	@Override
	public void startEntity(final String name) {
		entityStack.push(name);
		appendEntityToPath();
	}

	@Override
	public void endEntity() {
		removeEntityFromPath();
		entityStack.pop();
	}

	@Override
	public void closeStream() {
		clearStackAndPath();
	}

	@Override
	public void resetStream() {
		clearStackAndPath();
	}

	private void clearStackAndPath() {
		entityStack.clear();
		currentPath.setLength(0);
	}

	private void appendEntityToPath() {
		if (entityStack.size() > 1) {
			currentPath.append(entitySeparator);
		}
		currentPath.append(entityStack.peek());
	}

	private void removeEntityFromPath() {
		final String entityName = entityStack.peek();
		final int oldPathLength = currentPath.length();
		int lastEntityLength = entityName.length();
		if (entityStack.size() > 1) {
			lastEntityLength += entitySeparator.length();
		}
		currentPath.setLength(oldPathLength - lastEntityLength);
	}

}
