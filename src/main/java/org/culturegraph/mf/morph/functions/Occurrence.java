/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.morph.functions;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.util.StringUtil;


/**
 * @author Markus Michael Geipel
 *
 */
public final class Occurrence extends AbstractStatefulFunction {

	private static final String MORE_THEN = "moreThen ";
	private static final String LESS_THEN = "lessThen ";
	private int count;
	private IntFilter filter = new IntFilter() {
		@Override
		public boolean accept(final int value) {
			return true;
		}
	};
	private String format;
	
	private final Map<String, String> variables = new HashMap<String, String>();
	private boolean sameEntity;
	


	@Override
	public String process(final String value) {
		++count;
		if(filter.accept(count)){
			return processMatch(value);
		}
		return null;
	}
	
	private String processMatch(final String value) {
		if(format==null){
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
		count=0;
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
		
		if(only.startsWith(LESS_THEN)){
			final int number = Integer.parseInt(only.substring(LESS_THEN.length()));
			filter = new IntFilter() {
				@Override
				public boolean accept(final int value) {
					return value < number;
				}
			};
		}else if (only.startsWith(MORE_THEN)){
			final int number = Integer.parseInt(only.substring(MORE_THEN.length()));
			filter = new IntFilter() {
				@Override
				public boolean accept(final int value) {
					return value > number;
				}
			};
		}else{
			final int number = Integer.parseInt(only);
			filter = new IntFilter() {
				@Override
				public boolean accept(final int value) {
					return value == number;
				}
			};
		}
		return filter;
	}

	/**
	 *	Filter for integer values
	 */
	private interface IntFilter{
		boolean accept(int value);
	}
}
