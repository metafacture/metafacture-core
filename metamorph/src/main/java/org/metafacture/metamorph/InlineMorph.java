/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.metamorph;

import org.metafacture.framework.StreamReceiver;

import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.URL;

/**
 * Helper for including Metamorph scripts directly in Java code.
 * <p>
 * Typically, {@code InlineMoprh} is used as shown in the example to create a
 * {@link Metamorph} instance based on an inline script:
 * <pre>{@code
 * Metamorph metamorph = InlineMorph.in(this)
 *     .with("<rules>")
 *     .with("<data source='test' />")
 *     .with("</rules>")
 *     .create();
 * }</pre>
 * <p>
 * The morph script can either be complete xml document or a snippet containing
 * only the inner part of the {@code <metamorph>} element.
 * <p>
 * The two different variants are distinguished by checking whether the script
 * starts with &ldquo;{@literal <?xml }&rdquo; or
 * &ldquo;{@literal <metamorph }&rdquo;. Leading whitespace is ignored. If it
 * starts with an xml declaration, it is assumed that it is a
 * complete xml document and the script is passed to {@link Metamorph} without
 * modification. If it starts with a {@code <metamorph>} element, an xml
 * declaration is added. If it starts with neither, the script is wrapped in a
 * {@code <metamorph>} element and an xml declaration is added.
 * <p>
 * This class is primarily intended to simplify testing of Metamorph
 * extensions.
 *
 * @author Christoph Böhme
 */
public final class InlineMorph {

    private static final String XML_BOILERPLATE = "<?xml version='1.1' encoding='UTF-8'?>\n";
    private static final String METAMORPH_BOILERPLATE = "<metamorph version='1'\n    xmlns='http://www.culturegraph.org/metamorph'>";
    private static final String FINISH_METAMORPH_BOILERPLATE = "</metamorph>\n";

    private final StringBuilder scriptBuilder = new StringBuilder();

    private String systemId;

    private boolean needFinishMetamorphBoilerplate;

    private InlineMorph() {
        // Instance may only be created via #in(Object) or #in(Class)
    }

    /**
     * Sets the object which contains the morph script.
     * <p>
     * This is an alternative to the {@link #in(Class)} method and has been
     * defined for convenience.
     *
     * @param owner the object which contains the morph script
     * @return an instance of {@code InlineMorph} for continuation
     */
    public static InlineMorph in(final Object owner) {
        return in(owner.getClass());
    }

    /**
     * Sets the class which contains the morph script. It will be used to create
     * a system identifier for the morph script. The identifier is created by
     * constructing a resource name from the class's package and converting it
     * into a url. This allows inline scripts to refer to files that are placed
     * in the same package as the class.
     *
     * @param owner the class which contains the morph script
     * @return an instance of {@code InlineMorph} for continuation
     */
    public static InlineMorph in(final Class<?> owner) {
        return new InlineMorph().setClassAsSystemId(owner);
    }

    private InlineMorph setClassAsSystemId(final Class<?> owner) {
        final URL baseUrl = owner.getResource("");
        systemId = baseUrl.toExternalForm();
        return this;
    }

    /**
     * Adds a line to the morph script.
     *
     * @param line the next line of the morph script
     * @return a reference to {@code this} for continuation
     */
    public InlineMorph with(final String line) {
        if (scriptBuilder.length() == 0) {
            appendBoilerplate(line);
        }
        scriptBuilder
                .append(line)
                .append("\n");
        return this;
    }

    private void appendBoilerplate(final String line) {
        final String trimmedLine = line.trim();
        if (!trimmedLine.startsWith("<?xml ")) {
            appendXmlBoilerplate();
            if (!trimmedLine.startsWith("<metamorph ")) {
                appendMetamorphBoilerplate();
                needFinishMetamorphBoilerplate = true;
            }
        }
    }

    private void appendXmlBoilerplate() {
        scriptBuilder.append(XML_BOILERPLATE);
    }

    private void appendMetamorphBoilerplate() {
        scriptBuilder.append(METAMORPH_BOILERPLATE);
    }

    /**
     * Creates a {@link Metamorph} instance.
     *
     * @return a Metamorph object initialised with the inline script
     */
    public Metamorph create() {
        finishBoilerplate();
        return new Metamorph(createScriptSource());
    }

    private void finishBoilerplate() {
        if (needFinishMetamorphBoilerplate) {
            scriptBuilder.append(FINISH_METAMORPH_BOILERPLATE);
            needFinishMetamorphBoilerplate = false;
        }
    }

    private InputSource createScriptSource() {
        final InputSource scriptSource = new InputSource();
        scriptSource.setSystemId(systemId);
        scriptSource.setCharacterStream(new StringReader(toString()));
        return scriptSource;
    }

    /**
     * Returns the string representation of this Morph script.
     *
     * @return the Morph script
     */
    @Override
    public String toString() {
        return scriptBuilder.toString() + (needFinishMetamorphBoilerplate ? FINISH_METAMORPH_BOILERPLATE : "");
    }

    /**
     * Creates a {@link Metamorph} instance. The {@code Metamorph} instance will
     * be connected to the receiver passed as argument.
     * <p>
     * This is a convenience method. It is equivalent to calling {@link #create()}
     * to create a {@link Metamorph} object and then call
     * {@link Metamorph#setReceiver(StreamReceiver)} on the returned object.
     *
     * @param receiver downstream module to which the metamorph instance should
     *                  send its output.
     * @return a Metamorph object initialised with the inline script
     */
    public Metamorph createConnectedTo(final StreamReceiver receiver) {
        final Metamorph metamorph = create();
        metamorph.setReceiver(receiver);
        return metamorph;
    }

}
