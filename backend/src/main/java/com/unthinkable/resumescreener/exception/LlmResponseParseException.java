package com.unthinkable.resumescreener.exception;

/**
 * Thrown when the LLM gateway call succeeds but the model's content could not be
 * parsed into the structured shape the caller requested (bad JSON, out-of-range values).
 */
public class LlmResponseParseException extends RuntimeException {
    public LlmResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmResponseParseException(String message) {
        super(message);
    }
}
