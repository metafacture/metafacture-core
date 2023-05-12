/*
 * Copyright 2023 Fabian Steeg, hbz
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.triples.AbstractTripleSort.Compare;

/**
 * Provide a user-friendly way to list all paths available for processing in fix (see also {@link MetafixListValues}).
 *
 * @author Fabian Steeg
 */
@Description("Lists all paths found in the input records. These paths can be used in a Fix to address fields. Options: " +
        "count (output occurence frequency of each path, sorted by highest frequency first; default: true), " +
        "template (for formatting the internal triple structure; default: ${s}\t|\t${o} if count is true, else ${s})" +
        "index (output individual repeated subfields and array elements with index numbers instead of '*'; default: false)")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("fix-list-paths")
public class MetafixListPaths extends MetafixStreamAnalyzer {

    public MetafixListPaths() {
        super("nothing()", Compare.PREDICATE);
        setIndex(false);
    }

    public void setIndex(final boolean index) {
        super.getFix().setEntityMemberName(index ? Metafix.DEFAULT_ENTITY_MEMBER_NAME : "*");
    }

    public boolean getIndex() {
        return super.getFix().getEntityMemberName().equals(Metafix.DEFAULT_ENTITY_MEMBER_NAME);
    }
}
