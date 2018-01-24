/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import java.io.Reader;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;


/**
 * Creates a reader for the supplied string and sends it to the receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Creates a reader for the supplied string and sends it to the receiver")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("read-string")
public final class StringReader
        extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    @Override
    public void process(final String str) {
        getReceiver().process(new java.io.StringReader(str));
    }

}
