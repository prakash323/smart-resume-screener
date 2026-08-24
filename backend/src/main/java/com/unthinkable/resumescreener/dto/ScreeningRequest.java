package com.unthinkable.resumescreener.dto;

import jakarta.validation.constraints.NotNull;

public record ScreeningRequest(
        @NotNull(message = "resumeId is required") Long resumeId,
        @NotNull(message = "jobDescriptionId is required") Long jobDescriptionId,
        String model
) {
}
