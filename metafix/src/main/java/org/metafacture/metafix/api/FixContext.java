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

package org.metafacture.metafix.api;

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.RecordTransformer;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface FixContext {

    /**
     * Executes the Fix context.
     *
     * @param metafix           the Metafix instance
     * @param record            the record
     * @param params            the parameters
     * @param options           the options
     * @param recordTransformer the record transformer
     */
    void execute(Metafix metafix, Record record, List<String> params, Map<String, String> options, RecordTransformer recordTransformer);

}
