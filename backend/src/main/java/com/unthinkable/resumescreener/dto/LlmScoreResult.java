package com.unthinkable.resumescreener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Raw score + justification the LLM returns when comparing a resume against a job description.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmScoreResult(
        int score,
        String justification
) {
}
