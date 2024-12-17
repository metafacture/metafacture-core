/*
* Copyright 2024 hbz
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

package org.metafacture.strings;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Creates a variable for the supplied string and continues the process.
*
* @author Tobias Bülte
*/
@Description("Creates a variable for the supplied string and continues the process.")
@In(String.class)
@Out(String.class)
@FluxCommand("string-to-variable")
public final class StringReader extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private final Map<String, String> vars = new HashMap<String, String>();
    public static final String variableName = "inputString";

    /**
     * Creates an instance of {@link StringVariable}.
    */
    public StringVariable() {
    }

    @Override
    public void process(final String str) {
        vars.put(variableName,str);
        getReceiver().process(str);
    }

}
