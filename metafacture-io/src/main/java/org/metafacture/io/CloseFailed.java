package org.metafacture.io;

public class CloseFailed extends IoFailed {

    public CloseFailed(final String message) {
        super(message);
    }

    public CloseFailed(final Throwable cause) {
        super(cause);
    }

    public CloseFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
