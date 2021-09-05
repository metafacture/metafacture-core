package org.metafacture.io;

public class WriteFailed extends IoFailed {

    public WriteFailed(final String message) {
        super(message);
    }

    public WriteFailed(final Throwable cause) {
        super(cause);
    }

    public WriteFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
