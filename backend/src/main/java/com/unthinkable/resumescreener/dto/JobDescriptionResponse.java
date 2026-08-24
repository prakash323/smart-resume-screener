package com.unthinkable.resumescreener.dto;

import java.time.Instant;

public record JobDescriptionResponse(
        Long id,
        String title,
        String rawText,
        Instant createdAt
) {
}
