/*
 * Copyright 2021 Fabian Steeg, hbz
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

import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metafix.method.field.*; // checkstyle-disable-line AvoidStarImport
import org.metafacture.metafix.method.record.*; // checkstyle-disable-line AvoidStarImport
import org.metafacture.metafix.method.script.*; // checkstyle-disable-line AvoidStarImport

import java.util.List;
import java.util.Map;

public enum FixMethod implements FixFunction { // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity

    // SCRIPT-LEVEL METHODS:

    include {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Include().apply(metafix, record, params, options);
        }
    },
    log {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Log().apply(metafix, record, params, options);
        }
    },
    nothing {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Nothing().apply(metafix, record, params, options);
        }
    },
    put_filemap {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new PutFileMap().apply(metafix, record, params, options);
        }
    },
    put_map {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new PutMap().apply(metafix, record, params, options);
        }
    },
    put_rdfmap {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new PutRdfMap().apply(metafix, record, params, options);
        }
    },
    put_var {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new PutVar().apply(metafix, record, params, options);
        }
    },
    put_vars {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new PutVars().apply(metafix, record, params, options);
        }
    },
    to_var {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new ToVar().apply(metafix, record, params, options);
        }
    },

    // RECORD-LEVEL METHODS:

    add_array {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new AddArray().apply(metafix, record, params, options);
        }
    },
    add_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new AddField().apply(metafix, record, params, options);
        }
    },
    add_hash {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new AddHash().apply(metafix, record, params, options);
        }
    },
    array { // array-from-hash
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Array().apply(metafix, record, params, options);
        }
    },
    call_macro {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new CallMacro().apply(metafix, record, params, options);
        }
    },
    copy_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new CopyField().apply(metafix, record, params, options);
        }
    },
    format {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Format().apply(metafix, record, params, options);
        }
    },
    hash { // hash-from-array
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Hash().apply(metafix, record, params, options);
        }
    },
    move_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new MoveField().apply(metafix, record, params, options);
        }
    },
    parse_text {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new ParseText().apply(metafix, record, params, options);
        }
    },
    paste {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Paste().apply(metafix, record, params, options);
        }
    },
    print_record {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new PrintRecord().apply(metafix, record, params, options);
        }
    },
    random {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Random().apply(metafix, record, params, options);
        }
    },
    reject {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Reject().apply(metafix, record, params, options);
        }
    },
    remove_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new RemoveField().apply(metafix, record, params, options);
        }
    },
    rename {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Rename().apply(metafix, record, params, options);
        }
    },
    retain {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Retain().apply(metafix, record, params, options);
        }
    },
    set_array {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new SetArray().apply(metafix, record, params, options);
        }
    },
    set_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new SetField().apply(metafix, record, params, options);
        }
    },
    set_hash {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new SetHash().apply(metafix, record, params, options);
        }
    },
    timestamp {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Timestamp().apply(metafix, record, params, options);
        }
    },
    vacuum {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Vacuum().apply(metafix, record, params, options);
        }
    },

    // FIELD-LEVEL METHODS:

    // TODO SPEC: switch to morph-style named params in general?

    append {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Append().apply(metafix, record, params, options);
        }
    },
    capitalize {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Capitalize().apply(metafix, record, params, options);
        }
    },
    count {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Count().apply(metafix, record, params, options);
        }
    },
    downcase {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Downcase().apply(metafix, record, params, options);
        }
    },
    filter {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Filter().apply(metafix, record, params, options);
        }
    },
    flatten {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Flatten().apply(metafix, record, params, options);
        }
    },
    from_json {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new FromJson().apply(metafix, record, params, options);
        }
    },
    index {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Index().apply(metafix, record, params, options);
        }
    },
    isbn {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Isbn().apply(metafix, record, params, options);
        }
    },
    join_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new JoinField().apply(metafix, record, params, options);
        }
    },
    lookup {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Lookup().apply(metafix, record, params, options);
        }
    },
    prepend {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Prepend().apply(metafix, record, params, options);
        }
    },
    replace_all {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new ReplaceAll().apply(metafix, record, params, options);
        }
    },
    reverse {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Reverse().apply(metafix, record, params, options);
        }
    },
    sort_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new SortField().apply(metafix, record, params, options);
        }
    },
    split_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new SplitField().apply(metafix, record, params, options);
        }
    },
    substring {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Substring().apply(metafix, record, params, options);
        }
    },
    sum {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Sum().apply(metafix, record, params, options);
        }
    },
    to_base64 {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new ToBase64().apply(metafix, record, params, options);
        }
    },
    to_json {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new ToJson().apply(metafix, record, params, options);
        }
    },
    trim {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Trim().apply(metafix, record, params, options);
        }
    },
    uniq {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Uniq().apply(metafix, record, params, options);
        }
    },
    upcase {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new Upcase().apply(metafix, record, params, options);
        }
    },
    uri_encode {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            new UriEncode().apply(metafix, record, params, options);
        }
    }

}
