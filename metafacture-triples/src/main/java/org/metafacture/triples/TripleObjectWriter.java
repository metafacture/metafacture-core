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

package org.metafacture.triples;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectReceiver;
import org.metafacture.framework.objects.Triple;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writes the object value of the triple into a file. The filename
 * is constructed from subject and predicate.
 *
 * Please note: This module does not check if the filename constructed
 * from subject and predicate stays within {@code baseDir}. THIS MODULE
 * SHOULD NOT BE USED IN ENVIRONMENTS IN WHICH THE VALUES OF SUBJECT AND
 * PREDICATE A PROVIDED BY AN UNTRUSTED SOURCE!
 *
 * @author Christoph Böhme
 */
@Description("Writes the object value of the triple into a file. The filename is " +
    "constructed from subject and predicate. Please note: This module does " +
    "not check if the filename constructed from subject and predicate stays " +
    "within `baseDir`. THIS MODULE SHOULD NOT BE USED IN ENVIRONMENTS IN WHICH " +
    "THE VALUES OF SUBJECT AND PREDICATE A PROVIDED BY AN UNTRUSTED SOURCE!")
@In(Triple.class)
@Out(Void.class)
@FluxCommand("write-triple-objects")
public final class TripleObjectWriter extends DefaultObjectReceiver<Triple> {

    private final Path baseDir;

    private Charset encoding = StandardCharsets.UTF_8;

    /**
     * Creates an instance of {@link TripleObjectWriter} with a given directory to
     * write to.
     *
     * @param baseDir the path to the base directory
     */
    public TripleObjectWriter(final String baseDir) {
        this.baseDir = Paths.get(baseDir);
    }

    /**
     * Sets the encoding used to open the resource.
     *
     * @param encoding
     *            new encoding
     */
    public void setEncoding(final String encoding) {
        this.encoding = Charset.forName(encoding);
    }

    /**
     * Sets the encoding used to open the resource.
     *
     * @param encoding
     *            new encoding
     */
    public void setEncoding(final Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the encoding used to open the resource.
     *
     * @return current default setting
     */
    public String getEncoding() {
        return encoding.name();
    }

    @Override
    public void process(final Triple triple) {
        final Path filePath = buildFilePath(triple);
        ensureParentPathExists(filePath);
        try (Writer writer = Files.newBufferedWriter(filePath, encoding)) {
            writer.write(triple.getObject());
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private Path buildFilePath(final Triple triple) {
        final Path file = Paths.get(triple.getSubject(), triple.getPredicate());
        return baseDir.resolve(file).toAbsolutePath().normalize();
    }

    private void ensureParentPathExists(final Path path) {
        final Path parentDir = path.getParent();
        if (parentDir != null) {
            try {
                Files.createDirectories(parentDir);
            }
            catch (final IOException e) {
                throw new MetafactureException(e);
            }
        }
    }

}
