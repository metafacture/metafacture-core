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
package org.culturegraph.mf.statistics;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;


/**
 * Counts the number of records and fields read. Used mainly for test cases and
 * debugging.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 * @author Michael Büchner
 *
 */
@Description("Counts the number of records and fields read.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("stream-count")
public final class Counter extends DefaultStreamPipe<StreamReceiver> {

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

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		++numRecords;
		if(getReceiver() != null) {
			getReceiver().startRecord(identifier);
		}
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		++numEntities;
		if(getReceiver() != null) {
			getReceiver().startEntity(name);
		}
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		++numLiterals;
		if(getReceiver() != null) {
			getReceiver().literal(name, value);
		}
	}

	@Override
	public void endRecord() {
		if(getReceiver() != null) {
			getReceiver().endRecord();
		}
	}

	@Override
	public void endEntity() {
		if(getReceiver() != null) {
			getReceiver().endEntity();
		}
	}

	@Override
	public void onResetStream() {
		numRecords = 0;
		numEntities = 0;
		numLiterals = 0;
	}

	@Override
	public String toString() {
		String streamClosed = "";
		if (isClosed()) {
			streamClosed =" Stream has been closed.";
		}

		return "counted " + numRecords + " records, " + numEntities + " entities, " + numLiterals + " literals." + streamClosed;
	}
}
