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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class ValueTest {

    public ValueTest() {
    }

    @Test
    public void shouldSatisfyEqualsContract() {
        EqualsVerifier.forClass(Value.class)
            .withPrefabValues(Value.class, Value.newArray(), Value.newHash())
            .withPrefabValues(Value.Hash.class, Value.newHash().asHash(), Value.newHash(h -> h.put("k", new Value("v"))).asHash())
            .verify();
    }

}
