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
package org.culturegraph.mf.stream.sink;

import org.culturegraph.mf.framework.DefaultStreamReceiver;


/**
 * Counts the number of records and fields read. Used mainly for test cases and
 * debugging.
 * 
 * @author Markus Michael Geipel, Christoph Böhme
 * 
 */
public final class Counter extends DefaultStreamReceiver {
	
	private boolean closed;
	private int numRecords;
	private int numEntities;
	private int numLiterals;
	
	/**
	 * @return the numRecords
	 */
	public int getNumRecords() {
		return numRecords;
	}

	/**
	 * @return the numEntities
	 */
	public int getNumEntities() {
		return numEntities;
	}

	/**
	 * @return the numLiterals
	 */
	public int getNumLiterals() {
		return numLiterals;
	}
	
	/**
	 * 
	 * @return true if the stream is closed
	 */
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void startRecord(final String identifier) {
		++numRecords;
	}

	@Override
	public void startEntity(final String name) {
		++numEntities;
	}

	@Override
	public void literal(final String name, final String value) {
		++numLiterals;
	}

	@Override
	public void resetStream() {
		closed = false;
		numRecords = 0;
		numEntities = 0;
		numLiterals = 0;
	}
	
	@Override
	public void closeStream() {
		closed = true;
	}
	
	@Override
	public String toString() {
		String streamClosed = "";
		if (closed) {
			streamClosed =" Stream has been closed.";
		}
		
		return "counted " + numRecords + " records, " + numEntities + " entities, " + numLiterals + " literals." + streamClosed;
	}
}
