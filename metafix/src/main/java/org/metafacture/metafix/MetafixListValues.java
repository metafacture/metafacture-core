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
 * Provide a user-friendly way to list all values for a given path (see {@link MetafixListPaths}).
 *
 * @author Fabian Steeg
 */
@Description("Lists all values found for the given path. The paths can be found using fix-list-paths. Options: " +
        "count (output occurence frequency of each value, sorted by highest frequency first; default: true)" +
        "template (for formatting the internal triple structure; default: ${o}\t|\t${s} if count is true, else ${s})")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("fix-list-values")
public class MetafixListValues extends MetafixStreamAnalyzer {

    public MetafixListValues(final String path) {
        super(fix(path), Compare.OBJECT);
    }

    private static String fix(final String path) {
        return
            "copy_field(\"" + path + "\",\"value.$append\")\n" +
            "retain(\"value\")";
    }

}
