package com.example.betkickapi.exception;

/**
 * Exception thrown to indicate that a username already exists.
 */
public class UsernameAlreadyExistsException extends Exception {

    /**
     * Constructs an {@code UsernameAlreadyExistsException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
