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

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.annotations.ReturnsAvailableArguments;
import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.reflection.ObjectFactory;

/**
 * prints Flux help for a given {@link ObjectFactory}
 * 
 * @author Markus Michael Geipel
 * 
 */
public final class HelpPrinter {
	private HelpPrinter() {
		// no instances
	}

	public static void print(final ObjectFactory<?> factory, final PrintStream out) {
		
	
		out.println("WELCOME TO METAFACTURE");
		out.println(ResourceUtil.loadProperties("build.properties").toString());
		
		out.println("\nUsage:\tflux FLOW_FILE [VARNAME=VALUE ...]\n");
		out.println("Available pipe elements:\n");

		final List<String> keyWords = new ArrayList<String>();
		keyWords.addAll(factory.keySet());
		Collections.sort(keyWords);
		for (String name : keyWords) {
			describe(name, factory, out);
		}

	}

	private static <T> void  describe(final String name, final ObjectFactory<T> factory, final PrintStream out) {
		final Class<? extends T> clazz = factory.getClass(name);
		final Description desc = clazz.getAnnotation(Description.class);

		out.println(name);

		if (desc != null) {
			out.println("description:\t" + desc.value());
		}
		final Collection<String> arguments = getAvailableArguments(clazz);
		if (!arguments.isEmpty()) {
			out.println("argument in\t" + arguments);
		}
		
		final Map<String, Class<?>> attributes = factory.getAttributes(name);
				
		if (!attributes.isEmpty()) {
			out.print("options:\t");
			final StringBuilder builder = new StringBuilder();
			for (Entry<String, Class<?>> entry: attributes.entrySet()) {
				if (entry.getValue().isEnum()) {
					builder.append(entry.getKey() + " " + Arrays.toString(entry.getValue().getEnumConstants())	+", ");
				}else{
					builder.append(entry.getKey() + " (" + entry.getValue().getName()+"), ");
				}
				
			}
			out.println(builder.substring(0, builder.length()-2));
		}

		out.println("implementation:\t" + clazz.getCanonicalName());
		String inString = "<unknown>";
		String outString = "";
		final In inClass = clazz.getAnnotation(In.class);
		if (inClass != null) {
			inString = inClass.value().getCanonicalName();
		}
		final Out outClass = clazz.getAnnotation(Out.class);
		if (outClass != null) {
			outString = outClass.value().getCanonicalName();
		}
		out.println("signature:\t" + inString + " -> " + outString);
		out.println();
	}

	@SuppressWarnings("unchecked")
	private static Collection<String> getAvailableArguments(final Class<?> clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.getAnnotation(ReturnsAvailableArguments.class) != null) {
				try {
					return (Collection<String>) method.invoke(clazz, new Object[0]);
				} catch (IllegalAccessException e) {
					// silently ignore
				} catch (IllegalArgumentException e) {
					// silently ignore
				} catch (InvocationTargetException e) {
					// silently ignore
				}
			}
		}
		return Collections.emptyList();
	}
}
