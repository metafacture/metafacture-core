package org.metafacture.fix;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class FixStandaloneSetup extends FixStandaloneSetupGenerated {

    public FixStandaloneSetup() {
    }

    public static void doSetup() {
        new FixStandaloneSetup().createInjectorAndDoEMFRegistration();
    }

}
