package com.nerdcoffe.exception;

public class UnverifiedAccountException extends RuntimeException {
    
    public UnverifiedAccountException(String message) {
        super(message);
    }

    public UnverifiedAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
