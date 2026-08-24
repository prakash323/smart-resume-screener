package com.unthinkable.resumescreener.exception;

public class ResumeParsingException extends RuntimeException {
    public ResumeParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResumeParsingException(String message) {
        super(message);
    }
}
