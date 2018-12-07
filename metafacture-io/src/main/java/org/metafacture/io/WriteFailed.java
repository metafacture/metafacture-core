package org.metafacture.io;

public class WriteFailed extends IoFailed {

    public WriteFailed(String message) {
        super(message);
    }

    public WriteFailed(Throwable cause) {
        super(cause);
    }

    public WriteFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
