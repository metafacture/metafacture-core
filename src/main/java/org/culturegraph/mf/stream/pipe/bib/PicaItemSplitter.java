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
package org.culturegraph.mf.stream.pipe.bib;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Extract items from PICA records.
 * 
 * @author Christoph Böhme
 *
 */
@Description("Extract items from PICA records.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class PicaItemSplitter extends DefaultStreamPipe<StreamReceiver> {

	private static final char SUFFIX_SEPARATOR = '/';
	
	private String currentSuffix;
	private String identifier;
	
	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		this.currentSuffix = null;
		this.identifier = identifier;
		getReceiver().startRecord(identifier);
	}
	
	@Override
	public void endRecord() {
		assert !isClosed();		
		getReceiver().endRecord();
	}
	
	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		int suffixStart = name.lastIndexOf(SUFFIX_SEPARATOR);
		if (suffixStart == -1) {
			suffixStart = name.length();
		}
		final String suffix = name.substring(suffixStart);
		if (currentSuffix != null) {
			if (!currentSuffix.equals(suffix)) {
				getReceiver().endRecord();
				getReceiver().startRecord(identifier);
			}
		}
		currentSuffix = suffix;
		getReceiver().startEntity(name.substring(0, suffixStart));
	}
	
	@Override 
	public void endEntity() {
		assert !isClosed();		
		getReceiver().endEntity();
	}
	
	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();		
		getReceiver().literal(name, value);
	}
	
}
