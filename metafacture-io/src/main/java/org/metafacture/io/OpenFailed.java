package org.metafacture.io;

public class OpenFailed extends IoFailed {

    public OpenFailed(String message) {
        super(message);
    }

    public OpenFailed(Throwable cause) {
        super(cause);
    }

    public OpenFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
