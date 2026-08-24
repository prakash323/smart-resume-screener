package com.unthinkable.resumescreener.exception;

/**
 * Thrown when the LLM gateway call itself fails (network error, non-2xx response, timeout).
 */
public class LlmException extends RuntimeException {
    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmException(String message) {
        super(message);
    }
}
