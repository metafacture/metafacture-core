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

package org.metafacture.formatting;

import org.metafacture.commons.StringUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.framework.objects.Triple;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a {@link String} from a template and an {@link Object}. `${o}` marks
 * the place where the object is to be inserted. If the received object is an
 * instance of Triple`${s}`, `${p}` and `${o}` are used instead.
 *
 * @param <T>
 *            object type
 *
 * @author Markus Geipel
 *
 */
@Description("Builds a String from a template and an Object. Provide template in brackets. `${o}` marks the place where the object is to be inserted. " +
        "If the object is an instance of Triple `${s}`, `${p}` and `${o}` are used instead.")
@In(Object.class)
@Out(String.class)
@FluxCommand("template")
public final class ObjectTemplate<T> extends DefaultObjectPipe<T, ObjectReceiver<String>> {

    //TODO: make replace more efficient
    private static final Pattern OBJ_PATTERN = Pattern.compile("${o}", Pattern.LITERAL);
    private final Map<String, String> vars = new HashMap<>();
    private final String template;

    /**
     * Creates an instance of {@link ObjectTemplate} with a given template.
     *
     * @param template the template
     */
    public ObjectTemplate(final String template) {
        this.template = template;
    }

    @Override
    public void process(final T obj) {
        if (obj instanceof Triple) {
            final Triple triple = (Triple) obj;
            vars.put("s", triple.getSubject());
            vars.put("p", triple.getPredicate());
            vars.put("o", triple.getObject());
            getReceiver().process(StringUtil.format(template, vars));
        }
        else {
            final Matcher matcher = OBJ_PATTERN.matcher(template);
            getReceiver().process(matcher.replaceAll(obj.toString()));
        }
    }
}
