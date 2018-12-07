package org.metafacture.io;

import org.metafacture.framework.MetafactureException;

public class IoFailed extends MetafactureException {

    public IoFailed(String message) {
        super(message);
    }

    public IoFailed(Throwable cause) {
        super(cause);
    }

    public IoFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
