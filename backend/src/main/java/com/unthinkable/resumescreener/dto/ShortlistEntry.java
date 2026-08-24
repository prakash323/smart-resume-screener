package com.unthinkable.resumescreener.dto;

public record ShortlistEntry(
        Long resumeId,
        String candidateName,
        String email,
        int score,
        String justification,
        String modelUsed
) {
}
