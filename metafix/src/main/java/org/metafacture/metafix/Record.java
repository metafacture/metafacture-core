/*
 * Copyright 2021 hbz NRW
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

package org.metafacture.metafix;

/**
 * Represents a metadata record, i.e., a {@link Value.Hash Hash} of fields
 * and values.
 */
public class Record extends Value.Hash {

    private boolean reject;

    /**
     * Creates an empty instance of {@link Record}.
     */
    public Record() {
    }

    /**
     * Returns a shallow clone of this record.
     *
     * @return a new record pre-populated with all entries from this record
     */
    public Record shallowClone() {
        final Record clone = new Record();

        clone.setReject(reject);
        forEach(clone::put);

        return clone;
    }

    /**
     * Flags whether this record should be rejected.
     *
     * @param reject true if this record should not be emitted, false otherwise
     */
    public void setReject(final boolean reject) {
        this.reject = reject;
    }

    /**
     * Checks whether this record should be rejected.
     *
     * @return true if this record should not be emitted, false otherwise
     */
    public boolean getReject() {
        return reject;
    }

    @Override
    public String toString() {
        // TODO: Improve string representation? Include reject status, etc.?
        return super.toString();
    }

}
