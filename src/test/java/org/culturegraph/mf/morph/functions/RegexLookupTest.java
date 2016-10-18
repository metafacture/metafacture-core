/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.morph.functions;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Ignore;

import org.apache.commons.io.FileUtils;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.converter.xml.SimpleXmlEncoder;
import org.culturegraph.mf.stream.reader.CsvReader;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.util.ResourceUtil;
import org.junit.Test;

@Ignore
public class RegexLookupTest {
	
	public final static String MORPH_DEFINITION = "morph/regexlookup.morph.xml";
	public final static String EXAMPLE_DATA_CSV = "data/regexlookup.csv";
	public final static String RESULT_XML = "test/regexlookup.result.xml";
	
	@Test
	public void testRegexLookup() throws IOException {
		
		String inputFileName = ResourceUtil.getUrl(EXAMPLE_DATA_CSV).getPath();
		final Metamorph metamorph = new Metamorph(MORPH_DEFINITION);
		
		final FileOpener opener = new FileOpener();

		final CsvReader csvReader = new CsvReader(",");
		csvReader.setHasHeader(true);

		final SimpleXmlEncoder xmlEncoder = new SimpleXmlEncoder();	

		opener
		.setReceiver(csvReader)
		.setReceiver(metamorph)
		.setReceiver(xmlEncoder)
		;	
		
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> streamWriter = new ObjectJavaIoWriter<String>(stringWriter);
		
		xmlEncoder.setReceiver(streamWriter);
		opener.process(inputFileName);
		
		String result = stringWriter.toString();
		
		final File expected = new File(ResourceUtil.getUrl(RESULT_XML).getPath());
		assertEquals(FileUtils.readFileToString(expected), result);
	}

}