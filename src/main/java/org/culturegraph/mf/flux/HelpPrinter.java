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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.annotations.ReturnsAvailableArguments;
import org.culturegraph.mf.util.reflection.ObjectFactory;



/**
 * prints Flux help for a given {@link ObjectFactory}
 * 
 * @author Markus Michael Geipel
 *
 */
public final class HelpPrinter {
	private HelpPrinter() {
		//no instances
	}
	
	public static void print(final ObjectFactory<?> factory) {
		System.err.println("Usage:\tMetaflow FLOW_FILE [VARNAME=VALUE ...]\n");
		System.err.println("Available pipe elements:\n");
		
		final List<String> keyWords = new ArrayList<String>();
		keyWords.addAll(factory.keySet());
		Collections.sort(keyWords);
		for (String name : keyWords) {
			describe(name, factory);
		}
		
	}

	private static void describe(final String name, final ObjectFactory<?> factory) {
		final Class<?> clazz = factory.getClass(name);
		final Description desc = clazz.getAnnotation(Description.class);
		System.err.println(name);
		
		if (desc != null) {
			System.err.println("description:\t" + desc.value());
		}
		final Collection<String> arguments = getAvailableArguments(clazz);
		if(!arguments.isEmpty()){
			System.err.println("argument in\t" + arguments);
		}
		final Collection<String> options = factory.getAttributes(name);
		if(!options.isEmpty()){
			System.err.println("options:\t" + options);
		}
		
		System.err.println("implementation:\t" + clazz.getCanonicalName());
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
		System.err.println("signature:\t" + inString + " -> " + outString);
		System.err.println();
	}
	
	@SuppressWarnings("unchecked")
	private static Collection<String> getAvailableArguments(final Class<?> clazz){
		for (Method method : clazz.getMethods()) {
			if(method.getAnnotation(ReturnsAvailableArguments.class)!=null){
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
