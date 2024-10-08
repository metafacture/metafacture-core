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

package org.metafacture.metamorph.functions;

import org.metafacture.commons.StringUtil;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.helpers.AbstractFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs regexp matching.
 *
 * @author Markus Michael Geipel
 */
public final class Regexp extends AbstractFunction {

    private Matcher matcher;
    private String format;
    private final Map<String, String> tempVars = new HashMap<String, String>();

    /**
     * Creates an instance of {@link Regexp}.
     */
    public Regexp() {
    }

    @Override
    public void receive(final String name, final String value, final NamedValueSource source, final int recordCount, final int entityCount) {
        if (null == value) {
            return;
        }
        matcher.reset(value);
        if (null == format) {
            while (matcher.find()) {
                final String group = matcher.group();
                if (!group.isEmpty()) {
                    getNamedValueReceiver().receive(name, group, this,
                            recordCount, entityCount);
                }
            }
        }
        else {
            while (matcher.find()) {
                populateVars();
                if (!tempVars.isEmpty()) {
                    getNamedValueReceiver().receive(name,
                            StringUtil.format(format, tempVars), this,
                            recordCount, entityCount);
                }
            }
        }
    }

    private void populateVars() {
        tempVars.clear();
        for (int i = 0; i <= matcher.groupCount(); ++i) {
            final String group = matcher.group(i);
            if (group != null && !group.isEmpty()) {
                tempVars.put(String.valueOf(i), group);
            }
        }
    }

    /**
     * Sets the matcher.
     *
     * @param match the matcher
     */
    public void setMatch(final String match) {
        matcher = Pattern.compile(match).matcher("");
    }

    /**
     * Sets the format.
     *
     * @param format the format
     */
    public void setFormat(final String format) {
        this.format = format;
    }

}
