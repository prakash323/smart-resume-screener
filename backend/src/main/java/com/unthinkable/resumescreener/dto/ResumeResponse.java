package com.unthinkable.resumescreener.dto;

import java.time.Instant;

public record ResumeResponse(
        Long id,
        String candidateName,
        String email,
        String fileName,
        ExtractedResumeData extractedData,
        Instant createdAt
) {
}
