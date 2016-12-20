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
package org.culturegraph.mf.metamorph.xml;

import java.util.Deque;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Annotates a DOM with information about the location of
 * the elements in the input file.
 *
 * This class is intended to be used with a transformer which
 * reads a SAX source and writes to a DOM.
 *
 * @author Christoph Böhme
 *
 */
final class LocationAnnotator extends XMLFilterImpl {

	private static final String DOM_NODE_INSERTED = "DOMNodeInserted";

	private final Deque<Node> domNodes = new LinkedList<Node>();
	private final Deque<Locator> locatorsAtElementStart = new LinkedList<Locator>();

	private Locator locator;

	public LocationAnnotator(final XMLReader xmlReader, final Document document) {
		super(xmlReader);

		final EventListener listener = new EventListener() {

			@Override
			public void handleEvent(final Event event) {
				final Node node = (Node) event.getTarget();
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					domNodes.push(node);
				}
			}

		};

		((EventTarget) document).addEventListener(DOM_NODE_INSERTED, listener, true);
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		super.setDocumentLocator(locator);
		this.locator = locator;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes atts) throws SAXException {

		super.startElement(uri, localName, qName, atts);

		locatorsAtElementStart.push(new LocatorImpl(locator));
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {

		super.endElement(uri, localName, qName);

		final Locator elementStartLocator = locatorsAtElementStart.pop();

		final Location location = new Location(elementStartLocator, locator);

		final Node domNode = domNodes.pop();
		domNode.setUserData(Location.USER_DATA_ID, location, Location.USER_DATA_HANDLER);
	}

}
