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
package org.culturegraph.mf.metamorph.xml;

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Stores location information about element start and element
 * end positions in a document.
 *
 * @author Christoph Böhme
 *
 */
public final class Location {

	public static final String USER_DATA_ID = "http://culturegraph.org/mf/user_data/location";

	public static final UserDataHandler USER_DATA_HANDLER = new UserDataHandler() {

		@Override
		public void handle(final short operation, final String key, final Object data,
				final Node src, final Node dst) {

			if (operation == NODE_IMPORTED || operation == NODE_CLONED) {
				if (dst != null) {
					dst.setUserData(key, data, this);
				}
			}
		}

	};

	private final Locator elementStart;
	private final Locator elementEnd;

	public Location(final Locator elementStart, final Locator elementEnd) {
		this.elementStart = new LocatorImpl(elementStart);
		this.elementEnd = new LocatorImpl(elementEnd);
	}

	public Location(final Location src) {
		elementStart = new LocatorImpl(src.elementStart);
		elementEnd = new LocatorImpl(src.elementEnd);
	}

	public Locator getElementStart() {
		return elementStart;
	}

	public Locator getElementEnd() {
		return elementEnd;
	}

	public String getSystemId() {
		return elementStart.getSystemId();
	}

	public String getPublicId() {
		return elementStart.getPublicId();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getSystemId());
		builder.append(':');
		builder.append(elementStart.getLineNumber());
		builder.append(':');
		builder.append(elementStart.getColumnNumber());
		builder.append(" - ");
		builder.append(elementEnd.getLineNumber());
		builder.append(':');
		builder.append(elementEnd.getColumnNumber());

		return builder.toString();
	}

}
