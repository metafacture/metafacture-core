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

package org.metafacture.framework.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * Stores an immutable subject-predicate-object triple.
 *
 * @author Markus Michael Geipel
 */
public final class Triple implements Comparable<Triple> {

    private final String subject;
    private final String predicate;
    private final String object;
    private final ObjectType objectType;

    private final int preCompHashCode;

    /**
     * Creates an instance of {@link Triple}.
     *
     * @param subject   the subject
     * @param predicate the predicate
     * @param object    the object
     */
    public Triple(final String subject, final String predicate,
            final String object) {
        this(subject, predicate, object, ObjectType.STRING);
    }

    /**
     * Constructs a Triple.
     *
     * @param subject    the subject
     * @param predicate  the predicate
     * @param object     the object
     * @param objectType the ObjectType
     */
    public Triple(final String subject, final String predicate,
            final String object, final ObjectType objectType) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.objectType = objectType;
        preCompHashCode = computeHashCode();
    }

    private int computeHashCode() {
        return Objects.hash(subject, predicate, object, objectType);
    }

    /**
     * Gets the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the predicate
     *
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * Gets the object.
     *
     * @return the object
     */
    public String getObject() {
        return object;
    }

    /**
     * Gets the object type.
     *
     * @return the {@link ObjectType}
     */
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * Reads an ObjectInputStream as Triple and returns it.
     *
     * @param in the ObjectInputStream
     * @return the Triple
     * @throws IOException if an I/O error occurs
     */
    public static Triple read(final ObjectInputStream in) throws IOException {
        try {
            return new Triple(in.readUTF(), in.readUTF(), in.readUTF(),
                    (ObjectType) in.readObject());
        }
        catch (final ClassNotFoundException e) {
            throw new IOException("Cannot read triple", e);
        }
    }

    /**
     * Writes the Triple to an ObjectOutputStream.
     *
     * @param out the ObjectOutputStream.
     * @throws IOException if an I/O error occurs
     */
    public void write(final ObjectOutputStream out) throws IOException {
        out.writeUTF(subject);
        out.writeUTF(predicate);
        out.writeUTF(object);
        out.writeObject(objectType);
    }

    @Override
    public int hashCode() {
        return preCompHashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Triple)) {
            return false;
        }
        final Triple other = (Triple) obj;
        return other.preCompHashCode == preCompHashCode &&
                other.predicate.equals(predicate) &&
                other.object.equals(object) &&
                other.subject.equals(subject) &&
                other.objectType == objectType;
    }

    @Override
    public int compareTo(final Triple triple) {
        int result = subject.compareTo(triple.subject);
        if (result == 0) {
            result = predicate.compareTo(triple.predicate);
            if (result == 0) {
                result = object.compareTo(triple.object);
                if (result == 0) {
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

    /**
     * Content type of triple object.
     */
    public enum ObjectType {
        STRING, ENTITY
    }

}
