package org.metafacture.metafix;

import org.metafacture.framework.MetafactureException;
import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.validation.XtextValidator;

import com.google.common.io.CharStreams;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class FixStandaloneSetup extends FixStandaloneSetupGenerated {

    public FixStandaloneSetup() {
    }

    public static void main(final String[] args) {
        if (args != null && args.length == 1) {
            System.exit(XtextValidator.validate(args[0], new FixStandaloneSetup()) ? 0 : 1);
        }

        throw new IllegalArgumentException(String.format("Usage: %s <fix-file>", FixStandaloneSetup.class.getName()));
    }

    public static Fix parseFix(final Reader fixDef) {
        final String path;

        try {
            path = absPathToTempFile(fixDef, ".fix");
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }

        return (Fix) XtextValidator.getValidatedResource(path, new FixStandaloneSetup()).getContents().get(0);
    }

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
