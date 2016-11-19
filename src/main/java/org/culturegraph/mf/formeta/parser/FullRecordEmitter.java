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
package org.culturegraph.mf.formeta.parser;

import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Emits full records.
 *
 * @author Christoph Böhme
 *
 */
public final class FullRecordEmitter implements Emitter {

	private StreamReceiver receiver;

	@Override
	public void setReceiver(final StreamReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void startGroup(final String name, final int nestingLevel) {
		if (nestingLevel == 0) {
			receiver.startRecord(name);
		} else {
			receiver.startEntity(name);
		}
	}

	@Override
	public void endGroup(final int nestingLevel) {
		if (nestingLevel == 0) {
			receiver.endRecord();
		} else {
			receiver.endEntity();
		}
	}

	@Override
	public void literal(final String name, final String value, final int nestingLevel) {
		if (nestingLevel == 0) {
			throw new FormatException("literals may only appear in records");
		}
		receiver.literal(name, value);
	}

}
