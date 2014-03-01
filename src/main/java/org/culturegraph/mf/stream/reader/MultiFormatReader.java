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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.annotations.ReturnsAvailableArguments;


/**
 * {@link MultiFormatReader} uses the {@link AbstractReaderFactory} to handle
 * all registered input formats.
 * 
 * @author Markus Michael Geipel
 */

@Description("Reads different formats. Format given in brackets.")
@In(java.io.Reader.class)
@Out(StreamReceiver.class)
public final class MultiFormatReader implements Reader{
	private static final ReaderFactory READER_FACTORY = new ReaderFactory();
	
	private static final String ERROR_NO_FORMAT = "no format set";
	private static final String ERROR_RECEIVER_NULL = "'streamReceiver' must not be null";
	private Reader currentReader;
	private final Map<String, Reader> openReaders = new HashMap<String, Reader>();
	
	private StreamReceiver streamReceiver;
	private String currentFormat;

	public MultiFormatReader() {
		//nothing
	}
	
	public MultiFormatReader(final String format) {
		setFormat(format);
	}

	@ReturnsAvailableArguments
	public static Set<String> getAvailableFormats(){
		return READER_FACTORY.keySet();
	}

	public String getFormat() {
		return currentFormat;
	}

	public void setFormat(final String format) {
		if (format == null) {
			throw new IllegalArgumentException("'format' must not be null");
		}

		currentReader = openReaders.get(format);
		currentFormat = format;

		if (null == currentReader) {
			if(!READER_FACTORY.containsKey(format)){
				throw new IllegalArgumentException("Format '" + format + "' not regognized.");
			}
			currentReader = READER_FACTORY.newInstance(format);
			
			openReaders.put(format, currentReader);

			if (streamReceiver != null) {
				currentReader.setReceiver(streamReceiver);
			}
		}
	}

	@Override
	public <R extends StreamReceiver> R setReceiver(final R streamReceiver) {
		if (streamReceiver == null) {
			throw new IllegalArgumentException(ERROR_RECEIVER_NULL);
		}

		this.streamReceiver = streamReceiver;

			for (Reader reader : openReaders.values()) {
				reader.setReceiver(streamReceiver);
			}
		return streamReceiver;
	}

	@Override
	public void read(final String entry) {
		if (streamReceiver == null) {
			throw new IllegalStateException(ERROR_NO_FORMAT);
		}
		currentReader.read(entry);
	}


	@Override
	public void process(final java.io.Reader reader) {
		currentReader.process(reader);

	}

	@Override
	public void resetStream() {
		currentReader.resetStream();
	}

	@Override
	public void closeStream() {
		currentReader.closeStream();
	}
}
