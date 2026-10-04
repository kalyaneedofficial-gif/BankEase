package com.bankease.util;

/** Thrown when a withdrawal or transfer would violate the account's minimum balance. */
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
