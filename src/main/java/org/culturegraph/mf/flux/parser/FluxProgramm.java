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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.commons.reflection.ConfigurableClass;
import org.culturegraph.mf.commons.reflection.ObjectFactory;
import org.culturegraph.mf.commons.reflection.ReflectionUtil;
import org.culturegraph.mf.flux.FluxParseException;
import org.culturegraph.mf.flux.HelpPrinter;
import org.culturegraph.mf.framework.Receiver;

/**
 * @author Markus Michael Geipel
 *
 */
public final class FluxProgramm {

	private static final ObjectFactory<Receiver> COMMAND_FACTORY = new ObjectFactory<Receiver>();
	private static final String PROPERTIES_LOCATION = "flux-commands.properties";

	static {
		try {
			final Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader()
					.getResources(PROPERTIES_LOCATION);
			while (enumeration.hasMoreElements()) {
				final URL url = enumeration.nextElement();
				COMMAND_FACTORY.loadClassesFromMap(ResourceUtil.loadProperties(url), Receiver.class);
			}
		} catch (final IOException e) {
			throw new FluxParseException("unable to load properties.", e);
		}
	}

	private Flow currentFlow = new Flow();
	private final List<Flow> initialFlows = new ArrayList<Flow>();
	private final Map<String, Wormhole> wormholeNameMapping = new HashMap<String, Wormhole>();
	private final Map<Flow, Wormhole> wormholeInFlowMapping = new Hashtable<Flow, Wormhole>();

	private static Receiver createElement(final String name, final Map<String, String> namedArgs,
			final List<Object> cArgs) {

		final Receiver newElement;
		if (COMMAND_FACTORY.containsKey(name)) {
			newElement = COMMAND_FACTORY.newInstance(name, namedArgs, cArgs.toArray());

		} else {
			final ConfigurableClass<? extends Receiver> elementClass =
					ReflectionUtil.loadClass(name, Receiver.class);
			newElement = elementClass.newInstance(namedArgs, cArgs.toArray());
		}
		return newElement;
	}

	protected void addElement(final String name, final Map<String, String> namedArgs, final List<Object> cArgs) {
		currentFlow.addElement(createElement(name, namedArgs, cArgs));
	}

	protected void startTee() {
		currentFlow.startTee();
	}

	protected void endTee() {
		currentFlow.endTee();
	}

	protected void endSubFlow() {
		currentFlow.endSubFlow();
	}

	protected void setStringStart(final String string) {
		currentFlow.setStringStart(string);
		if (!initialFlows.contains(currentFlow)) {
			initialFlows.add(currentFlow);
		}
	}

	protected void setStdInStart() {
		currentFlow.setStdInStart();
		if (!initialFlows.contains(currentFlow)) {
			initialFlows.add(currentFlow);
		}
	}

	protected void setWormholeEnd(final String name) {

		Wormhole wormhole = wormholeNameMapping.get(name);
		if (wormhole == null) {
			wormhole = new Wormhole(name);
			wormholeNameMapping.put(name, wormhole);
		}
		wormhole.addIn(currentFlow);
		wormholeInFlowMapping.put(currentFlow, wormhole);

	}

	protected void setWormholeStart(final String name) {

		if (initialFlows.contains(currentFlow)) {
			initialFlows.remove(currentFlow);
		}
		Wormhole wormhole = wormholeNameMapping.get(name);
		if (wormhole == null) {
			wormhole = new Wormhole(name);
			wormholeNameMapping.put(name, wormhole);
		}
		wormhole.setOut(currentFlow);

	}

	protected void nextFlow() {
		currentFlow = new Flow();
	}

	protected void compile() {

		for (final Wormhole wormhole : wormholeNameMapping.values()) {
			if (wormhole.getOut() == null) {
				throw new FluxParseException("Wormhole " + wormhole.getName() + " is going nowhere");
			}

			for (final Flow flow : wormhole.getIns()) {

				flow.addElement(wormhole.getOut().getFirst());
			}
		}
	}

	public void start() {
		for (final Flow flow : initialFlows) {
			flow.start();
			if (!wormholeInFlowMapping.containsKey(flow)) {
				flow.close();
			} else {
				wormholeInFlowMapping.get(flow).finished(flow);
			}
		}
	}

	public static void printHelp(final PrintStream out) {
		HelpPrinter.print(COMMAND_FACTORY, out);
	}

	private static final class Wormhole {
		private final Set<Flow> insReady = new HashSet<Flow>();
		private final Set<Flow> insFinished = new HashSet<Flow>();
		private Flow out;
		private final String name;

		public Wormhole(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Flow getOut() {
			return out;
		}

		public Set<Flow> getIns() {
			return insReady;
		}

		public void setOut(final Flow out) {
			this.out = out;
		}

		public void addIn(final Flow flow) {
			insReady.add(flow);
		}

		public void finished(final Flow flow) {
			insReady.remove(flow);
			insFinished.add(flow);
			if (insReady.isEmpty()) {
				for (final Flow finished : insFinished) {
					finished.close();
				}
			}
		}
	}

}
