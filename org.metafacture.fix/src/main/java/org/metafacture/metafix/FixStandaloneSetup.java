package org.metafacture.metafix;

import org.metafacture.metafix.fix.Fix;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import com.google.common.io.CharStreams;
import com.google.inject.Injector;

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

    public static void doSetup() {
        new FixStandaloneSetup().createInjectorAndDoEMFRegistration();
    }

    public static Fix parseFix(final Reader fixDef) {
        // TODO: do this only once per application
        final Injector injector = new FixStandaloneSetup().createInjectorAndDoEMFRegistration();
        FixStandaloneSetup.doSetup();

        try {
            final URI uri = URI.createFileURI(absPathToTempFile(fixDef, ".fix"));
            final Resource resource = injector.getInstance(XtextResourceSet.class).getResource(uri, true);
            final IResourceValidator validator = ((XtextResource) resource).getResourceServiceProvider().getResourceValidator();

            for (final Issue issue : validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl)) {
                System.err.println(issue.getMessage());
            }

            return (Fix) resource.getContents().get(0);
        }
        catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
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
