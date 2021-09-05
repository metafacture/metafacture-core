package org.metafacture.io;

import org.metafacture.framework.MetafactureException;

public class IoFailed extends MetafactureException {

    public IoFailed(final String message) {
        super(message);
    }

    public IoFailed(final Throwable cause) {
        super(cause);
    }

    public IoFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
