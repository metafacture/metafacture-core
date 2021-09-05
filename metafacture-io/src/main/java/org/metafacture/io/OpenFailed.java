package org.metafacture.io;

public class OpenFailed extends IoFailed {

    public OpenFailed(final String message) {
        super(message);
    }

    public OpenFailed(final Throwable cause) {
        super(cause);
    }

    public OpenFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
