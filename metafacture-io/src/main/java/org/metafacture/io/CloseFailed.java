package org.metafacture.io;

public class CloseFailed extends IoFailed {

    public CloseFailed(String message) {
        super(message);
    }

    public CloseFailed(Throwable cause) {
        super(cause);
    }

    public CloseFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
