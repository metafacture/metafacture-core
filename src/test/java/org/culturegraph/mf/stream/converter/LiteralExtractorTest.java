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
package org.culturegraph.mf.stream.converter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * @author Christoph Böhme
 *
 */
public final class LiteralExtractorTest {
	
	private static final String LITERAL_NAME = "extract_this";
	private static final String LITERAL_VALUE1 = "I've been extracted from a record";
	private static final String LITERAL_VALUE2 = "I've been extracted from a record, too";
	
	private LiteralExtractor extractor;
	
	@Mock
	private ObjectReceiver<String> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		extractor = new LiteralExtractor();
		extractor.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		extractor.closeStream();
	}
	
	@Test
	public void testShouldEmitLiteralValueAsObject() {
		extractor.setPattern(LITERAL_NAME);
		
		extractor.startRecord("");
		extractor.literal("L1", "V1");
		extractor.literal(LITERAL_NAME, LITERAL_VALUE1);
		extractor.literal("L2", "V2");
		extractor.endRecord();
		
		verify(receiver).process(LITERAL_VALUE1);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldEmitValueOfNestedLiteralsAsObject() {
		extractor.setPattern(LITERAL_NAME);
		
		extractor.startRecord("");
		extractor.startEntity("En1");
		extractor.literal(LITERAL_NAME, LITERAL_VALUE1);
		extractor.endEntity();
		extractor.endRecord();
		
		verify(receiver).process(LITERAL_VALUE1);
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldUseRegExForMatchingLiteralNames() {
		extractor.setPattern("^ex_\\d$");
		
		extractor.startRecord("");
		extractor.literal("ex_1", LITERAL_VALUE1);
		extractor.literal("L1", "V1");
		extractor.literal("ex_2", LITERAL_VALUE2);
		extractor.endRecord();
		
		verify(receiver).process(LITERAL_VALUE1);
		verify(receiver).process(LITERAL_VALUE2);
		verifyNoMoreInteractions(receiver);		
	}
}
