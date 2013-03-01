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
package org.culturegraph.mf.flux;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.culturegraph.mf.exceptions.FluxParseException;
import org.culturegraph.mf.framework.LifeCycle;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.Sender;
import org.culturegraph.mf.framework.Tee;
import org.culturegraph.mf.stream.source.StdInOpener;
import org.culturegraph.mf.stream.source.StringSender;
import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.reflection.ObjectFactory;



/**
 * @author Markus Michael Geipel
 * 
 */
public final class Flow {

	private static final ObjectFactory<LifeCycle> COMMAND_FACTORY = new ObjectFactory<LifeCycle>();
	private static final String PROPERTIES_LOCATION = "flux-commands.properties";

	private Deque<Tee<?>> teeStack = new LinkedList<Tee<?>>();
	private Deque<List<LifeCycle>> looseEndsStack = new LinkedList<List<LifeCycle>>();

	static {
		try {
			final Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader()
					.getResources(PROPERTIES_LOCATION);
			while (enumeration.hasMoreElements()) {
				final URL url = enumeration.nextElement();
				COMMAND_FACTORY.loadClassesFromMap(ResourceUtil.loadProperties(url), LifeCycle.class);
			}

		} catch (IOException e) {
			throw new FluxParseException("unable to load properties.", e);
		}
	}

	private LifeCycle element;
	private ObjectReceiver<? extends Object> start;
	private boolean joinLooseEnds;

	public LifeCycle createElement(final String name, final Map<String, String> namedArgs, final List<Object> cArgs) {

		final LifeCycle newElement;
		if (COMMAND_FACTORY.containsKey(name)) {
			newElement = COMMAND_FACTORY.newInstance(name, namedArgs, cArgs.toArray());

		} else {
			newElement = ObjectFactory.newInstance(ObjectFactory.loadClass(name, LifeCycle.class), cArgs.toArray());
			ObjectFactory.applySetters(newElement, namedArgs);
		}
		return newElement;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addElement(final LifeCycle nextElement) {
		if (element instanceof Sender) {
			final Sender sender = (Sender) element;
			if (joinLooseEnds) {
				teeStack.pop();
				for (LifeCycle looseEnd : looseEndsStack.pop()) {
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

	public void setStringStart(final String string) {
		start = new StringSender(string);
		element = start;
	}

	public void setStdInStart() {
		start = new StdInOpener();
		element = start;
	}

	public void start() {
		start.process(null);
		start.closeStream();
	}

	public static void printHelp() {
		HelpPrinter.print(COMMAND_FACTORY);
	}

}
