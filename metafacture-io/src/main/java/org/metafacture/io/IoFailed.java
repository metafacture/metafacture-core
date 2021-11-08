package org.metafacture.io;

import org.metafacture.framework.MetafactureException;

public class IoFailed extends MetafactureException {

    /**
     * Creates an instance of {@link IoFailed} by a given a message.
     *
     * @param message the message
     */
    public IoFailed(final String message) {
        super(message);
    }

    /**
     * Creates an instance of {@link IoFailed} by a given a cause.
     *
     * @param cause the {@link Throwable}
     */
    public IoFailed(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance of {@link IoFailed} by a given message and a cause.
     *
     * @param message the message
     * @param cause   the {@link Throwable}
     */
    public IoFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
