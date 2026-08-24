package com.unthinkable.resumescreener.dto;

import java.time.Instant;

public record ScreeningResponse(
        Long id,
        Long resumeId,
        Long jobDescriptionId,
        int score,
        String justification,
        String modelUsed,
        Instant createdAt
) {
}
