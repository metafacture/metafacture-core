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
package org.culturegraph.mf.csv;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

import com.opencsv.CSVReader;

/**
 * Decodes lines of CSV files. First line is interpreted as header.
 *
 * @author Markus Michael Geipel
 * @author Fabian Steeg (fsteeg)
 *
 */
@Description("Decodes lines of CSV files. First line is interpreted as header.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-csv")
public final class CsvDecoder extends DefaultObjectPipe<String, StreamReceiver>  {

	private static final char DEFAULT_SEP = ',';
	private final char separator;

	private String[] header = new String[0];
	private int count;
	private boolean hasHeader;

	/**
	 * @param separator to split lines
	 */
	public CsvDecoder(final char separator) {
		super();
		this.separator = separator;
	}

	public CsvDecoder() {
		super();
		this.separator = DEFAULT_SEP;
	}

	@Override
	public void process(final String string) {
		assert !isClosed();
		final String[] parts = parseCsv(string);
		if(hasHeader){
			if(header.length==0){
				header = parts;
			}else if(parts.length==header.length){
				getReceiver().startRecord(String.valueOf(++count));
				for (int i = 0; i < parts.length; ++i) {
					getReceiver().literal(header[i], parts[i]);
				}
				getReceiver().endRecord();
			}else{
				throw new IllegalArgumentException(
						String.format(
								"wrong number of columns (expected %s, was %s) in input line: %s",
								header.length, parts.length, string));
			}
		}else{
			getReceiver().startRecord(String.valueOf(++count));
			for (int i = 0; i < parts.length; ++i) {
				getReceiver().literal(String.valueOf(i), parts[i]);
			}
			getReceiver().endRecord();
		}
	}

	private String[] parseCsv(final String string) {
		String[] parts = new String[0];
		try {
			final CSVReader reader = new CSVReader(new StringReader(string),
					separator);
			final List<String[]> lines = reader.readAll();
			if (lines.size() > 0) {
				parts = lines.get(0);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parts;
	}

	public void setHasHeader(final boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

}
