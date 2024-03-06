package com.leandroruhl.betkickapi.exception;

/**
 * Exception thrown to indicate an issue with the user's account balance.
 */
public class AccountBalanceException extends RuntimeException {

    /**
     * Constructs an {@code AccountBalanceException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public AccountBalanceException(String message) {
        super(message);
    }
}
