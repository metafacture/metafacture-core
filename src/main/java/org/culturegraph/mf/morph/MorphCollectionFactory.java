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

package org.culturegraph.mf.morph;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.culturegraph.mf.types.MultiHashMap;
import org.culturegraph.mf.types.MultiMap;


/**
 * Sets the implementation for different Collections used in {@link Metamorph}.
 * 
 * @author markus geipel
 *
 */
final class MorphCollectionFactory {
	private MorphCollectionFactory() {
		//no instances
	}
	
	public static MultiMap createMultiMap(){
		return new MultiHashMap();
	}
	
	public static  <T> List<T> createList(){
		return new ArrayList<T>();
	}
	
	public static <T> Deque<T> createDeque(){
		return new LinkedList<T>();
	}
	
	public static <T> Registry<T> createRegistry(){
		return new WildcardRegistry<T>();
	}
}
