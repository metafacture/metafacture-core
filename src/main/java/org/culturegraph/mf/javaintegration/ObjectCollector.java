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
package org.culturegraph.mf.javaintegration;

import java.util.LinkedList;
import java.util.Queue;

import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * Collects the objects emitted by an upstream module.
 *
 * @param <T> object type
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectCollector<T> implements ObjectReceiver<T> {

	private final Queue<T> buffer = new LinkedList<>();
	private final int maxCapacity;

	private boolean closed;

	public ObjectCollector() {
		this(-1);
	}

	public ObjectCollector(final int maxCapacity) {
		super();
		this.maxCapacity = maxCapacity;
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void process(final T obj) {
		if (closed) {
			throw new IllegalStateException("Module has been closed.");
		}
		if (buffer.size() == maxCapacity) {
			throw new IllegalStateException("Buffer reached max capacity.");
		}

		buffer.add(obj);
	}

	@Override
	public void resetStream() {
		buffer.clear();
		closed = false;
	}

	@Override
	public void closeStream() {
		closed = true;
	}

	/**
	 * Discards the current content of the buffer.
	 */
	public void clear() {
		buffer.clear();
	}

	/**
	 * Returns and removes the next item from the buffer.
	 *
	 * @return next item from the buffer.
	 */
	public T pop() {
		return buffer.poll();
	}

}
