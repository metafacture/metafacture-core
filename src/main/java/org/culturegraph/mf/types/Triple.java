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
package org.culturegraph.mf.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Stores an immutable name-value-pair. The hash code is 
 * precomputed during instantiation. 
 * 
 * @author Markus Michael Geipel
 */
public final class Triple  implements Comparable<Triple> {
	
	private static final int MAGIC1 = 23;
	private static final int MAGIC2 = 31;
	private static final int MAGIC3 = 17;
	private final String subject;
	private final String predicate;
	private final String object;
	private final int preCompHashCode;
	
	public Triple(final String subject, final String predicate, final String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		int result = MAGIC1;
		result = MAGIC2 * result + predicate.hashCode();
		result = MAGIC2 * result + object.hashCode();
		result = MAGIC3 * result + subject.hashCode();
		preCompHashCode = result;
	}
	
	
	/**
	 * @return object
	 */
	public String getObject() {
		return object;
	}
	
	/**
	 * @return predicate
	 */
	public String getPredicate() {
		return predicate;
	}
	
	/**
	 * @return subject
	 */
	public String getSubject() {
		return subject;
	}
	
	public static Triple read(final ObjectInputStream in) throws IOException{
		return new Triple(in.readUTF(), in.readUTF(), in.readUTF());
	}
	
	public void write(final ObjectOutputStream out) throws IOException{
		out.writeUTF(subject);
		out.writeUTF(predicate);
		out.writeUTF(object);
	}

	@Override
	public int hashCode() {
		return preCompHashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Triple) {
			final Triple triple = (Triple) obj;
			return triple.preCompHashCode == preCompHashCode 
					&& triple.predicate.equals(predicate) 
					&& triple.object.equals(object)
					&& triple.subject.equals(subject);
		}
		return false;
	}

	@Override
	public int compareTo(final Triple namedValue) {
		int result = subject.compareTo(namedValue.subject);
		if (result == 0) {
			result = predicate.compareTo(namedValue.predicate);
			if(result == 0){
				return object.compareTo(namedValue.object);
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		return subject + ":" + predicate + "=" + object; 
	}
}