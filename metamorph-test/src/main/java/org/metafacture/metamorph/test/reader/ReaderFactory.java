/*
 * Copyright 2013, 2014, 2016 Deutsche Nationalbibliothek
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

package org.metafacture.metamorph.test.reader;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.commons.reflection.ObjectFactory;
import org.metafacture.framework.MetafactureException;

import java.io.IOException;

/**
 * Instantiates instances of the {@link Reader} interface.
 *
 * @author Christoph Böhme
 *
 */
final class ReaderFactory extends ObjectFactory<Reader> {

    ReaderFactory() {
        try {
            loadClassesFromMap(ResourceUtil.loadProperties("test-readers.properties"), Reader.class);
        }
        catch (final IOException e) {
            throw new MetafactureException("Failed to load readers list", e);
        }
    }

}
