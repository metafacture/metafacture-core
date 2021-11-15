package org.metafacture.metafix.ide;

import org.metafacture.metafix.FixRuntimeModule;
import org.metafacture.metafix.FixStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class FixIdeSetup extends FixStandaloneSetup {

    public FixIdeSetup() {
    }

    @Override
    public Injector createInjector() {
        return Guice.createInjector(Modules2.mixin(new FixRuntimeModule(), new FixIdeModule()));
    }

}
