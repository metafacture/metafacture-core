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

package org.metafacture.metafix.util;

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixPredicate;
import org.metafacture.metafix.conditional.AnyEqual;
import org.metafacture.metafix.conditional.Exists;

import java.util.List;
import java.util.Map;

public class TestPredicate implements FixPredicate {

    public TestPredicate() {
    }

    @Override
    public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        return !new Exists().test(metafix, record, params, options) ||
            new AnyEqual().test(metafix, record, params, options);
    }

}
