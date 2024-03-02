package com.example.betkickapi.exception;

/**
 * Exception thrown to indicate an invalid password.
 */
public class InvalidPasswordException extends Exception {

    /**
     * Constructs an {@code InvalidPasswordException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}
