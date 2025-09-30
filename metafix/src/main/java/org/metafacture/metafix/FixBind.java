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

import org.metafacture.metafix.api.FixContext;
import org.metafacture.metafix.bind.*; // checkstyle-disable-line AvoidStarImport

import java.util.List;
import java.util.Map;

public enum FixBind implements FixContext {

    list {
        @Override
        public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
            new org.metafacture.metafix.bind.List().execute(metafix, record, params, options, recordTransformer);
        }
    },

    list_as {
        @Override
        public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
            new ListAs().execute(metafix, record, params, options, recordTransformer);
        }
    },

    once {
        @Override
        public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
            new Once().execute(metafix, record, params, options, recordTransformer);
        }
    },

    put_macro {
        @Override
        public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
            new PutMacro().execute(metafix, record, params, options, recordTransformer);
        }
    }

}
