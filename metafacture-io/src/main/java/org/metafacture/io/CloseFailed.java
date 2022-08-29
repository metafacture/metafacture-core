package org.metafacture.io;

public class CloseFailed extends IoFailed {

    /**
     * Creates an instance of {@link CloseFailed} by a given message.
     *
     * @param message the message
     */
    public CloseFailed(final String message) {
        super(message);
    }

    /**
     * Creates an instance of {@link CloseFailed} by a given cause.
     *
     * @param cause the {@link Throwable}
     */
    public CloseFailed(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance of {@link CloseFailed} by a given message and a cause.
     *
     * @param message the message
     * @param cause   the {@link Throwable}
     */
    public CloseFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
