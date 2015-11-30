/*
 *  Copyright 2013, 2014 hbz
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
package org.culturegraph.mf.stream.converter.xml;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.DataFilePath;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Pascal Christoph (dr0i)
 */
public final class AlephMabXmlHandlerTest {

	private ResourceOpener opener;
	private XmlDecoder xmlDecoder;
	private AlephMabXmlHandler mabXmlHandler;

	@Mock
	private StreamReceiver receiver;

	@After
	public void cleanup() {
		this.opener.closeStream();
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.opener = new ResourceOpener();
		this.xmlDecoder = new XmlDecoder();
		this.mabXmlHandler = new AlephMabXmlHandler();
		this.opener.setReceiver(this.xmlDecoder).setReceiver(this.mabXmlHandler)
				.setReceiver(this.receiver);
	}

	@Test
	public void testShouldIgnoreCharDataNotInARecord() {

		this.opener.process(DataFilePath.ALEPH_MAB_XML);
		final InOrder ordered = Mockito.inOrder(this.receiver);
		ordered.verify(this.receiver).startRecord("");
		ordered.verify(this.receiver).startEntity("001-1");
		ordered.verify(this.receiver).literal("a", "HT010726584");
		ordered.verify(this.receiver).endEntity();
		ordered.verify(this.receiver).startEntity("331-1");
		ordered.verify(this.receiver).literal("a", "Physics of plasmas");
		ordered.verify(this.receiver).endEntity();
		ordered.verify(this.receiver).startEntity("902-1");
		ordered.verify(this.receiver).literal("s", "Zeitschrift");
		ordered.verify(this.receiver).literal("9", "(DE-588)4067488-5");
		ordered.verify(this.receiver).endEntity();
		ordered.verify(this.receiver).endRecord();
		ordered.verify(this.receiver).startRecord("");
		ordered.verify(this.receiver).startEntity("001-1");
		ordered.verify(this.receiver).literal("a", "HT018700720");
		ordered.verify(this.receiver).endEntity();
		ordered.verify(this.receiver).startEntity("100b1");
		ordered.verify(this.receiver).literal("p", "Amrhein, Ludwig");
		ordered.verify(this.receiver).endEntity();
		ordered.verify(this.receiver).endRecord();
	}

}
