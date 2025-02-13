package org.metafacture.metafix;

import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.validation.XtextValidator;

import com.google.common.io.CharStreams;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class FixStandaloneSetup extends FixStandaloneSetupGenerated {

    /**
     * Creates an instance of {@link FixStandaloneSetup}.
     */
    public FixStandaloneSetup() {
    }

    /**
     * {@link XtextValidator#validate(String, ISetup) Validates} the Fix file.
     * Exits with error code {@code 1} if validation fails.
     *
     * @param args the pathname of the Fix file to validate
     */
    public static void main(final String[] args) {
        if (args != null && args.length == 1) {
            System.exit(XtextValidator.validate(args[0], new FixStandaloneSetup()) ? 0 : 1);
        }

        throw new IllegalArgumentException(String.format("Usage: %s <fix-file>", FixStandaloneSetup.class.getName()));
    }

    /**
     * {@link XtextValidator#getValidatedResource(String, ISetup) Parses and validates}
     * the Fix file.
     *
     * @param path the pathname of the Fix file to parse
     *
     * @return the Fix instance
     */
    public static Fix parseFix(final String path) {
        return (Fix) XtextValidator.getValidatedResource(path, new FixStandaloneSetup()).getContents().get(0);
    }

    /**
     * {@link XtextValidator#getValidatedResource(String, ISetup) Parses and validates}
     * the Fix definition after storing it in a {@link #absPathToTempFile temporary file}.
     *
     * @param fixDef the Fix definition to parse
     *
     * @return the Fix instance
     */
    public static Fix parseFix(final Reader fixDef) {
        try {
            return parseFix(absPathToTempFile(fixDef, ".fix"));
        }
        catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Stores the Fix definition in a temporary file with the given suffix.
     *
     * @param fixDef the Fix definition
     * @param suffix the file suffix
     *
     * @return the path to the temporary file
     *
     * @throws IOException if an I/O error occurs
     */
    public static String absPathToTempFile(final Reader fixDef, final String suffix) throws IOException {
        // TODO: avoid temp file creation
        final File file = File.createTempFile("metafix", suffix);
        file.deleteOnExit();

        try (FileWriter out = new FileWriter(file)) {
            CharStreams.copy(fixDef, out);
        }

        return file.getAbsolutePath();
    }

}
