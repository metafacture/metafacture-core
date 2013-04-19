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
 * Stores an immutable name-value-pair. The hash code is precomputed during
 * instantiation.
 * 
 * @author Markus Michael Geipel
 */
public final class Triple implements Comparable<Triple> {

	/**
	 * Content type of triple object
	 */
	public enum ObjectType {
		STRING, ENTITY
	}

	private static final int MAGIC1 = 23;
	private static final int MAGIC2 = 31;
	private static final int MAGIC3 = 17;
	private final String subject;
	private final String predicate;
	private final String object;
	private final ObjectType objectType;

	private final int preCompHashCode;

	public Triple(final String subject, final String predicate, final String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		objectType = ObjectType.STRING;
		int result = MAGIC1;
		result = MAGIC2 * result + predicate.hashCode();
		result = MAGIC2 * result + object.hashCode();
		result = MAGIC3 * result + subject.hashCode();
		result = MAGIC3 * result + objectType.hashCode();
		preCompHashCode = result;
		

	}
	
	public Triple(final String subject, final String predicate, final String object, final ObjectType objectType) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.objectType = objectType;
		int result = MAGIC1;
		result = MAGIC2 * result + predicate.hashCode();
		result = MAGIC2 * result + object.hashCode();
		result = MAGIC3 * result + subject.hashCode();
		result = MAGIC3 * result + objectType.hashCode();
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
	 * @return object type
	 */
	public ObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @return subject
	 */
	public String getSubject() {
		return subject;
	}

	public static Triple read(final ObjectInputStream in) throws IOException {
		return new Triple(in.readUTF(), in.readUTF(), in.readUTF());
	}

	public void write(final ObjectOutputStream out) throws IOException {
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
			return triple.preCompHashCode == preCompHashCode && triple.predicate.equals(predicate)
					&& triple.object.equals(object) && triple.subject.equals(subject) && triple.objectType == objectType;
		}
		return false;
	}

	@Override
	public int compareTo(final Triple triple) {
		int result = subject.compareTo(triple.subject);
		if (result == 0) {
			result = predicate.compareTo(triple.predicate);
			if (result == 0) {
				result = object.compareTo(triple.object);
				if(result == 0){
					return objectType.compareTo(triple.objectType);
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return subject + ":" + predicate + "=" + object + " (" + objectType + ")";
	}
}