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

import org.culturegraph.mf.exceptions.WellformednessException;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * A stream receiver that throws an {@link WellformednessException} if 
 * the stream event methods are called in an invalid order. Additionally, 
 * the stream receiver checks that entity and literal names are not null.
 * 
 * @see StreamValidator
 * @see WellformednessException
 * 
 * @author Christoph Böhme
 * 
 */
public final class WellFormednessChecker implements StreamReceiver {
	
	private static final String NAME_MUST_NOT_BE_NULL = "name must not be null";
	
	private static final String NOT_IN_RECORD = "Not in record";
	private static final String NOT_IN_ENTITY = "Not in entity";
	private static final String IN_ENTITY = "In entity";
	private static final String IN_RECORD = "In record";
	
	private int nestingLevel;
	
	@Override
	public void startRecord(final String identifier) {
		if (nestingLevel > 0) {
			throw new WellformednessException(IN_RECORD);
		}
		nestingLevel += 1;
	}

	@Override
	public void endRecord() {
		if (nestingLevel < 1) { 
			throw new WellformednessException(NOT_IN_RECORD);
		} else if (nestingLevel > 1) {
			throw new WellformednessException(IN_ENTITY);			
		}
		nestingLevel -= 1;
	}

	@Override
	public void startEntity(final String name) {
		if (name == null) {
			throw new WellformednessException(NAME_MUST_NOT_BE_NULL);
		}
		if (nestingLevel < 1) {
			throw new WellformednessException(NOT_IN_RECORD);
		}
		nestingLevel += 1;
	}

	@Override
	public void endEntity() {
		if (nestingLevel < 2) {
			throw new WellformednessException(NOT_IN_ENTITY);
		}
		nestingLevel -= 1;
	}

	@Override
	public void literal(final String name, final String value) {
		if (name == null) {
			throw new WellformednessException(NAME_MUST_NOT_BE_NULL);
		}
		if (nestingLevel < 1) {
			throw new WellformednessException(NOT_IN_RECORD);
		}
	}
	
	@Override
	public void resetStream() {
		nestingLevel = 0;
	}

	@Override
	public void closeStream() {
		if (nestingLevel > 0) {
			throw new WellformednessException(IN_RECORD);
		}
	}

}
