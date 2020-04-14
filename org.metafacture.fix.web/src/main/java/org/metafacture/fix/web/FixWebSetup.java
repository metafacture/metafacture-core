package org.metafacture.fix.web;

import org.metafacture.fix.FixRuntimeModule;
import org.metafacture.fix.FixStandaloneSetup;
import org.metafacture.fix.ide.FixIdeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages in web applications.
 */
public class FixWebSetup extends FixStandaloneSetup {

    public FixWebSetup() {
    }

    @Override
    public Injector createInjector() {
        return Guice.createInjector(Modules2.mixin(new FixRuntimeModule(), new FixIdeModule(), new FixWebModule()));
    }

}
