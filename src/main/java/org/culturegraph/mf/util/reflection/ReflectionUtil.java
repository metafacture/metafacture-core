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
package org.culturegraph.mf.util.reflection;

import org.culturegraph.mf.exceptions.MetafactureException;


/**
 * Makes reflection easier. 
 * @author Markus Michael Geipel
 *
 */
public final class ReflectionUtil {
	private static final String INSTANTIATION_ERROR = " could not be instantiated";

	private ReflectionUtil() {
		// no instances
	}
	

	public static ClassLoader getClassLoader(){
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			throw new MetafactureException("Class loader could not be found.");
		}
		return loader;
	}

	public static Object instantiateClass(final String className) {
		if (className == null) {
			throw new IllegalArgumentException("'className' must not be null.");
		}
		try {
			final Class<?> clazz = getClassLoader().loadClass(className);
			return clazz.newInstance();
			
		} catch (ClassNotFoundException e) {
			throw new MetafactureException(className + INSTANTIATION_ERROR, e);
		} catch (InstantiationException e) {
			throw new MetafactureException(className + INSTANTIATION_ERROR, e);
		} catch (IllegalAccessException e) {
			throw new MetafactureException(className + INSTANTIATION_ERROR, e);
		}
	}

}
