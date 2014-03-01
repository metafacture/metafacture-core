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
package org.culturegraph.mf.stream.reader;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.converter.CsvDecoder;

/**
 * Reads Csv files. First line is interpreted as header.
 * 
 * @author Markus Geipel
 */
@Description("reads Csv files. First line is interpreted as header. Provide value separator in brackets as regexp.")
@In(java.io.Reader.class)
@Out(StreamReceiver.class)
public final class CsvReader extends ReaderBase<CsvDecoder> {

	public CsvReader() {
		super(new CsvDecoder());
	}
	
	public CsvReader(final String separator) {
		super(new CsvDecoder(separator));
	}
	
	public void setHasHeader(final boolean hasHeader){
		getDecoder().setHasHeader(hasHeader);
	}

}
