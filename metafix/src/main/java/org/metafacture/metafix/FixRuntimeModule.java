package org.metafacture.metafix;

import org.metafacture.metafix.interpreter.FixInterpreter;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class FixRuntimeModule extends AbstractFixRuntimeModule {

    public FixRuntimeModule() {
    }

    public Class<FixInterpreter> bindFixInterpreter() {
        return FixInterpreter.class;
    }

}
