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
package org.culturegraph.mf.metamorph.functions;

import java.util.ArrayList;
import java.util.List;

import org.culturegraph.mf.metamorph.api.NamedValueReceiver;
import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.helpers.AbstractFunction;


/**
 * Stores all received values and only releases them on flush.
 *
 * @author Markus Geipel
 *
 */
public final class Buffer extends AbstractFunction {

	private final List<Receipt> receipts = new ArrayList<Receipt>();
	private int currentRecord;

	@Override
	public void receive(final String name, final String value,
			final NamedValueSource source, final int recordCount,
			final int entityCount) {

		if (currentRecord != recordCount) {
			receipts.clear();
			currentRecord = recordCount;
		}

		receipts.add(new Receipt(name, value, this, recordCount, entityCount));

	}

	@Override
	public void flush(final int recordCount, final int entityCount) {

		for (final Receipt receipt : receipts) {
			receipt.send(getNamedValueReceiver());
		}
		receipts.clear();
	}

	/**
	 * buffer element
	 */
	private static final class Receipt {
		private final String name;
		private final String value;
		private final NamedValueSource source;
		private final int recordCount;
		private final int entityCount;

		protected Receipt(final String name, final String value,
				final NamedValueSource source, final int recordCount,
				final int entityCount) {
			this.name = name;
			this.value = value;
			this.source = source;
			this.recordCount = recordCount;
			this.entityCount = entityCount;
		}

		protected void send(final NamedValueReceiver receiver) {
			receiver.receive(name, value, source, recordCount, entityCount);
		}
	}

}
