package org.metafacture.metafix.validation;

import com.google.inject.Injector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class XtextValidator {

    private static final Logger LOG = LoggerFactory.getLogger(XtextValidator.class);

    private XtextValidator() {
        throw new IllegalAccessError("Utility class");
    }

    public static boolean validate(final URI uri) throws IOException {
        final Injector injector = new XtextStandaloneSetup().createInjectorAndDoEMFRegistration();
        final Resource resource = injector.getInstance(ResourceSet.class).getResource(uri, true);
        resource.load(null);

        final List<Issue> issues = injector.getInstance(IResourceValidator.class)
            .validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        final int count = issues.size();

        if (count > 0) {
            LOG.warn("The Xtext file '{}' has {} issue{}:", resource.getURI(), count, count > 1 ? "s" : "");

            for (final Issue issue : issues) {
                LOG.warn("- {}: {} ({}:{})", issue.getSeverity(), issue.getMessage(), issue.getLineNumber(), issue.getColumn());
            }

            return false;
        }

        return true;
    }

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException(String.format("Usage: %s <xtext-file>",
                        XtextValidator.class.getName()));
        }

        System.exit(validate(URI.createURI(args[0])) ? 0 : 1);
    }

}
