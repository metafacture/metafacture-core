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

package org.metafacture.plumbing;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.ForwardingStreamPipe;

/**
 * A simple pass-through module.
 *
 * @author Christoph Böhme
 */
@Description("A simple pass-through module")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("pass-through")
public final class IdentityStreamPipe extends ForwardingStreamPipe {

    /**
     * Creates an instance of {@link IdentityStreamPipe}.
     */
    public IdentityStreamPipe() {
    }

}
