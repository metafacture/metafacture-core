package org.metafacture.metafix.validation;

import org.metafacture.framework.MetafactureLogger;
import org.metafacture.metafix.FixParseException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class XtextValidator {

    private static final MetafactureLogger LOG = new MetafactureLogger(XtextValidator.class);

    private XtextValidator() {
        throw new IllegalAccessError("Utility class");
    }

    private static boolean validate(final XtextResource resource, final ISetup setup) {
        final List<Issue> issues = resource.getResourceServiceProvider()
            .getResourceValidator().validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);

        final int count = issues.size();

        if (count > 0) {
            LOG.warn("The {} file '{}' has {} issue{}:",
                    resourceType(setup), resource.getURI().toFileString(), count, count > 1 ? "s" : "");

            issues.forEach(i -> LOG.warn("- {}: {} ({}:{})",
                        i.getSeverity(), i.getMessage(), i.getLineNumber(), i.getColumn()));

            return false;
        }

        return true;
    }

    /**
     * Validates the resource.
     *
     * @param path  the path to the resource
     * @param setup the resource setup
     *
     * @return true if the resource is valid, otherwise false
     */
    public static boolean validate(final String path, final ISetup setup) {
        return validate(getResource(path, setup), setup);
    }

    private static XtextResource getResource(final String path, final ISetup setup) {
        final File file = new File(path);
        String absolutePath;

        try {
            absolutePath = file.getCanonicalPath();
        }
        catch (final IOException e) {
            absolutePath = file.getAbsolutePath();
        }

        return (XtextResource) setup.createInjectorAndDoEMFRegistration()
            .getInstance(XtextResourceSet.class).getResource(URI.createFileURI(absolutePath), true);
    }

    /**
     * Parses and validates the resource.
     *
     * @param path  the path to the resource
     * @param setup the resource setup
     *
     * @return the resource
     *
     * @throws FixParseException if the resource is invalid
     */
    public static XtextResource getValidatedResource(final String path, final ISetup setup) {
        final XtextResource resource = getResource(path, setup);

        if (validate(resource, setup)) {
            return resource;
        }
        else {
            throw new FixParseException("Invalid " + resourceType(setup) + " resource: " + path);
        }
    }

    private static String resourceType(final ISetup setup) {
        return setup.getClass().getSimpleName();
    }

    /**
     * Validates the Xtext file. Exits with error code {@code 1} if validation
     * fails.
     *
     * @param args the pathname of the Xtext file to validate
     */
    public static void main(final String[] args) {
        if (args != null && args.length == 1) {
            System.exit(validate(args[0], new XtextStandaloneSetup()) ? 0 : 1);
        }

        throw new IllegalArgumentException(String.format("Usage: %s <xtext-file>", XtextValidator.class.getName()));
    }

}
