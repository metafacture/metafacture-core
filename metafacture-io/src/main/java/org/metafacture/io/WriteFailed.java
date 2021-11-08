package org.metafacture.io;

public class WriteFailed extends IoFailed {

    /**
     * Creates an instance of {@link WriteFailed} by given message.
     *
     * @param message the message
     */
    public WriteFailed(final String message) {
        super(message);
    }

    /**
     * Creates an instance of {@link WriteFailed} by given cause.
     *
     * @param cause the {@link Throwable}
     */
    public WriteFailed(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance of {@link WriteFailed} by given message and cause.
     *
     * @param message the message
     * @param cause   the {@link Throwable}
     */
    public WriteFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

}
