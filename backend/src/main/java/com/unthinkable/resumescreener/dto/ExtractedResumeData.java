package com.unthinkable.resumescreener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Structured data the LLM extracts from a raw resume text.
 * Matches the JSON schema requested in the extraction prompt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractedResumeData(
        List<String> skills,
        List<String> experience,
        List<String> education
) {
    public ExtractedResumeData {
        skills = skills == null ? List.of() : List.copyOf(skills);
        experience = experience == null ? List.of() : List.copyOf(experience);
        education = education == null ? List.of() : List.copyOf(education);
    }
}
