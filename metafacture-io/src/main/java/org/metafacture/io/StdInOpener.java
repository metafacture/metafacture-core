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

package org.metafacture.io;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Helper class to open stdin
 *
 * @author Markus Michael Geipel
 *
 */
@Description("Opens stdIn.")
@In(String.class)
@Out(java.io.Reader.class)
public final class StdInOpener extends DefaultObjectPipe<Object, ObjectReceiver<java.io.Reader>> {

    /**
     * Creates an instance of {@link StdInOpener}.
     */
    public StdInOpener() {
    }

    @Override
    public void process(final Object notUsed) {
        if (notUsed == null) {
            final BufferedReader stdin;
            try {
                stdin = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            }
            catch (final UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 not supported", e);
            }
            getReceiver().process(stdin);
        }
        else {
            throw new IllegalArgumentException("Parameter not used. Must be null");
        }
    }
}
