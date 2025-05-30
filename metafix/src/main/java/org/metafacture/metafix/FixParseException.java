/*
 * Copyright 2022 hbz NRW
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

import org.metafacture.framework.MetafactureException;

public class FixParseException extends MetafactureException {

    /**
     * Creates an instance of {@link FixParseException}.
     *
     * @param message the exception message
     */
    public FixParseException(final String message) {
        super(message);
    }

    /**
     * Creates an instance of {@link FixParseException}.
     *
     * @param message the exception message
     * @param cause   the exception cause
     */
    public FixParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
