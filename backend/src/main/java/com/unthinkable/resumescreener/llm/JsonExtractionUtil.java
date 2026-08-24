package com.unthinkable.resumescreener.llm;

import com.unthinkable.resumescreener.exception.LlmResponseParseException;

/**
 * Pulls a single JSON object out of raw LLM text output. Models frequently wrap JSON in
 * markdown code fences or prepend a sentence before the object even when asked for
 * "JSON only" - this makes extraction robust to that instead of trusting the model.
 */
public final class JsonExtractionUtil {

    private JsonExtractionUtil() {
    }

    public static String extractJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new LlmResponseParseException("LLM response was empty");
        }

        int start = raw.indexOf('{');
        if (start == -1) {
            throw new LlmResponseParseException("No JSON object found in LLM response: " + truncate(raw));
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return raw.substring(start, i + 1);
                }
            }
        }

        throw new LlmResponseParseException("Unterminated JSON object in LLM response: " + truncate(raw));
    }

    private static String truncate(String s) {
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
