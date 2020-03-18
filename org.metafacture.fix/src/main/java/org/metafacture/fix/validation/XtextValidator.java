package org.metafacture.fix.validation;

import com.google.inject.Injector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import java.io.IOException;
import java.util.List;

public class XtextValidator {

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException(String.format("Usage: %s <xtext-file>",
                        XtextValidator.class.getName()));
        }

        final Injector injector = new XtextStandaloneSetup().createInjectorAndDoEMFRegistration();

        final Resource resource = injector.getInstance(ResourceSet.class)
            .getResource(URI.createURI(args[0]), true);
        resource.load(null);

        final List<Issue> issues = injector.getInstance(IResourceValidator.class)
            .validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        final int count = issues.size();

        if (count > 0) {
            System.out.println(String.format("The Xtext file '%s' has %d issue%s:\n",
                        resource.getURI(), count, count > 1 ? "s" : ""));

            for (final Issue issue : issues) {
                System.out.println(String.format("- %s: %s (%d:%d)",
                            issue.getSeverity(), issue.getMessage(), issue.getLineNumber(), issue.getColumn()));
            }
        }

        System.exit(count);
    }

}
