package com.unthinkable.resumescreener.dto;

import jakarta.validation.constraints.NotBlank;

public record JobDescriptionRequest(
        @NotBlank(message = "title must not be blank") String title,
        @NotBlank(message = "rawText must not be blank") String rawText
) {
}
