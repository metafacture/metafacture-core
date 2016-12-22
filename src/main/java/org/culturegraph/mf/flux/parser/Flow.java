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
package org.culturegraph.mf.flux.parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.culturegraph.mf.flux.FluxParseException;
import org.culturegraph.mf.framework.LifeCycle;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.Receiver;
import org.culturegraph.mf.framework.Sender;
import org.culturegraph.mf.framework.Tee;
import org.culturegraph.mf.io.StdInOpener;


/**
 * @author Markus Michael Geipel
 *
 */
final class Flow {

	private final Deque<Tee<?>> teeStack = new LinkedList<Tee<?>>();
	private final Deque<List<LifeCycle>> looseEndsStack = new LinkedList<List<LifeCycle>>();

	private LifeCycle element;
	private ObjectReceiver<? extends Object> start;
	private boolean joinLooseEnds;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addElement(final Receiver nextElement) {
		if(element==null){
			setStart((ObjectReceiver<? extends Object>) nextElement);
			return;
		}
		if (element instanceof Sender) {
			final Sender sender = (Sender) element;
			if (joinLooseEnds) {
				teeStack.pop();
				for (final LifeCycle looseEnd : looseEndsStack.pop()) {
					if (looseEnd instanceof Tee) {
						((Tee) looseEnd).addReceiver(nextElement);
					} else {
						((Sender) looseEnd).setReceiver(nextElement);
					}
				}
				joinLooseEnds = false;
			} else {
				if (sender instanceof Tee) {
					((Tee) sender).addReceiver(nextElement);
				} else {
					sender.setReceiver(nextElement);
				}
			}
		} else {
			throw new FluxParseException(element.getClass().getCanonicalName() + "is not a sender");
		}
		element = nextElement;
	}

	public void startTee() {
		if (element instanceof Tee) {
			final Tee<?> tee = (Tee<?>) element;
			teeStack.push(tee);
			looseEndsStack.push(new ArrayList<LifeCycle>());
		} else {
			throw new FluxParseException("Flow cannot be split without a tee-element.");
		}
	}

	public void endTee() {
		joinLooseEnds = true;

	}

	public void endSubFlow() {
		looseEndsStack.peek().add(element);
		element = teeStack.peek();
	}

	private void setStart(final ObjectReceiver<? extends Object> start){
		this.start = start;
		element = start;
	}

	public void setStringStart(final String string) {
		setStart(new StringSender(string));
	}

	public void setStdInStart() {
		setStart(new StdInOpener());
	}

	public void start() {
		start.process(null);
	}

	public void close() {
		start.closeStream();
	}

	public Receiver getFirst() {
		return start;
	}
}
