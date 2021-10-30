package org.metafacture.io;

public class OpenFailed extends IoFailed {

    /**
     * Creates an instance of {@link OpenFailed} by a given message.
     *
     * @param message the message
     */
    public OpenFailed(final String message) {
        super(message);
    }

    /**
     * Creates an instance of {@link OpenFailed} by a given cause.
     *
     * @param cause the {@link Throwable}
     */
    public OpenFailed(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance of {@link OpenFailed} by a given message and a cause.
     *
     * @param message the message
     * @param cause   the {@link Throwable}
     */
    public OpenFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
