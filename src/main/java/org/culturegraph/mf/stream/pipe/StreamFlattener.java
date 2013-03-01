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
package org.culturegraph.mf.stream.pipe;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * flattens out entities in a stream by introducing dots in literal names.
 * @author Markus Michael Geipel
 * 
 */

@Description("flattens out entities in a stream by introducing dots in literal names")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class StreamFlattener extends DefaultStreamPipe<StreamReceiver> {
	
	public static final String DEFAULT_ENTITY_MARKER = ".";
	private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";

	private String entityMarker = DEFAULT_ENTITY_MARKER;
	private final Deque<String> entityStack = new LinkedList<String>();
	private final StringBuilder entityPath = new StringBuilder();
	private String currentEntityPath = "";
	
	public void setEntityMarker(final String entityMarker) {
		this.entityMarker = entityMarker;
	}

	public String getEntityMarker() {
		return entityMarker;
	}

	@Override
	public void startRecord(final String identifier) {
		entityStack.clear();
		currentEntityPath = "";
		if (entityPath.length() != 0) {
			entityPath.delete(0, entityPath.length());
		}
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		currentEntityPath = "";
		getReceiver().endRecord();

	}

	@Override
	public void startEntity(final String name) {
		entityStack.push(name);
		entityPath.append(name);
		entityPath.append(entityMarker);
		currentEntityPath = entityPath.toString();

	}

	@Override
	public void endEntity() {
		try {
			final int end = entityPath.length();
			final String name = entityStack.pop();
			entityPath.delete(end - name.length() - entityMarker.length(), end);
			currentEntityPath = entityPath.toString();
		} catch (NoSuchElementException exc) {
			throw new IllegalStateException(ENTITIES_NOT_BALANCED + ": " + exc.getMessage(), exc);
		}
	}

	@Override
	public void literal(final String name, final String value) {
		getReceiver().literal(currentEntityPath + name, value);
	}

	public String getCurrentEntityName() {
		return entityStack.peek();
	}

	public String getCurrentPath() {
		if(currentEntityPath.isEmpty()){
			return "";
		}
		return currentEntityPath.substring(0, currentEntityPath.length() - entityMarker.length());
	}
	
}
