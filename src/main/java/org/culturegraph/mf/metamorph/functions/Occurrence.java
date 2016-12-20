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

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.metamorph.api.MorphBuildException;
import org.culturegraph.mf.metamorph.api.helpers.AbstractStatefulFunction;

/**
 * Only outputs the received values in a certain range.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public final class Occurrence extends AbstractStatefulFunction {

	private static final String LESS_THAN = "lessThan ";
	private static final String MORE_THAN = "moreThan ";

	private int count;
	private String format;

	private IntFilter filter = new IntFilter() {
		@Override
		public boolean accept(final int value) {
			return true;
		}
	};

	private final Map<String, String> variables = new HashMap<String, String>();
	private boolean sameEntity;

	@Override
	public String process(final String value) {
		++count;
		if (filter.accept(count)) {
			return processMatch(value);
		}
		return null;
	}

	private String processMatch(final String value) {
		if (format == null) {
			return value;
		}
		variables.put("value", value);
		variables.put("count", String.valueOf(count));
		return StringUtil.format(format, variables);
	}

	public void setFormat(final String format) {
		this.format = format;
	}

	@Override
	protected void reset() {
		count = 0;
	}

	@Override
	protected boolean doResetOnEntityChange() {
		return sameEntity;
	}

	public void setOnly(final String only) {
		filter = parse(only);
	}

	public void setSameEntity(final boolean sameEntity) {
		this.sameEntity = sameEntity;
	}

	private static IntFilter parse(final String only) {
		final IntFilter filter;

		if (only.startsWith(LESS_THAN)) {
			filter = createLessThanFilter(extractNumberFrom(only));
		} else if (only.startsWith(MORE_THAN)) {
			filter = createGreaterThanFilter(extractNumberFrom(only));
		} else {
			final int number = Integer.parseInt(only);
			filter = createEqualsFilter(number);
		}
		return filter;
	}

	private static int extractNumberFrom(final String string) {
		final String[] tokens = string.split(" ", 2);
		if (tokens.length < 2) {
			throw new MorphBuildException("Invalid only string: " + string);
		}
		return Integer.parseInt(tokens[1]);
	}

	private static IntFilter createEqualsFilter(final int number) {
		return new IntFilter() {
			@Override
			public boolean accept(final int value) {
				return value == number;
			}
		};
	}

	private static IntFilter createLessThanFilter(final int number) {
		return new IntFilter() {
			@Override
			public boolean accept(final int value) {
				return value < number;
			}
		};
	}

	private static IntFilter createGreaterThanFilter(final int number) {
		return new IntFilter() {
			@Override
			public boolean accept(final int value) {
				return value > number;
			}
		};
	}

	/**
	 * Filter for integer values
	 */
	private interface IntFilter {
		boolean accept(int value);
	}

}
