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
package org.culturegraph.mf.metamorph;

import org.culturegraph.mf.metamorph.api.SourceLocation;
import org.culturegraph.mf.metamorph.xml.Location;

/**
 * Implementation of {@link SourceLocation} which is backed by {@link Location}
 * objects.
 *
 * @author Christoph Böhme
 */
final class XmlSourceLocation implements SourceLocation {

	private final Location location;

	XmlSourceLocation(final Location location) {
		this.location = location;
	}

	@Override
	public String getFileName() {
		return location.getSystemId();
	}

	@Override
	public Position getStartPosition() {
		return new Position() {
			@Override
			public int getLineNumber() {
				return location.getElementStart().getLineNumber();
			}

			@Override
			public int getColumnNumber() {
				return location.getElementStart().getColumnNumber();
			}
		};
	}

	@Override
	public Position getEndPosition() {
		return new Position() {
			@Override
			public int getLineNumber() {
				return location.getElementEnd().getLineNumber();
			}

			@Override
			public int getColumnNumber() {
				return location.getElementEnd().getColumnNumber();
			}
		};
	}

	@Override
	public String toString() {
		return location.toString();
	}

}
