package com.unthinkable.resumescreener.scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.dto.ExtractedResumeData;
import com.unthinkable.resumescreener.dto.LlmScoreResult;
import com.unthinkable.resumescreener.exception.LlmResponseParseException;
import com.unthinkable.resumescreener.llm.JsonExtractionUtil;
import com.unthinkable.resumescreener.llm.LlmClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Uses the LLM to compare a candidate resume against a job description and produce a
 * 1-10 fit score with a short justification, per the assignment's example prompt:
 * "Compare the following resume with this job description and rate fit on 1-10 with justification."
 */
@Service
public class ScoringService {

    static final String SYSTEM_PROMPT = """
            You are an expert technical recruiter. You objectively compare a candidate's résumé
            against a job description and score how well the candidate fits the role.
            You return ONLY a single JSON object - no markdown, no code fences, no commentary.
            Be specific in your justification: reference concrete skills or experience that
            matched or were missing, rather than generic praise.
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final OpenRouterProperties properties;

    public ScoringService(LlmClient llmClient, ObjectMapper objectMapper, OpenRouterProperties properties) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public LlmScoreResult score(String resumeText, ExtractedResumeData extracted, String jobDescriptionText, String modelOverride) {
        String model = (modelOverride == null || modelOverride.isBlank()) ? properties.getDefaultModel() : modelOverride;
        String userPrompt = buildUserPrompt(resumeText, extracted, jobDescriptionText);

        String rawResponse = llmClient.complete(SYSTEM_PROMPT, userPrompt, model);
        String json = JsonExtractionUtil.extractJsonObject(rawResponse);

        LlmScoreResult result;
        try {
            result = objectMapper.readValue(json, LlmScoreResult.class);
        } catch (MismatchedInputException e) {
            throw new LlmResponseParseException("Scoring response did not match the expected schema: " + json, e);
        } catch (IOException e) {
            throw new LlmResponseParseException("Failed to parse scoring response as JSON: " + json, e);
        }

        if (result.score() < 1 || result.score() > 10) {
            throw new LlmResponseParseException("Scoring response contained an out-of-range score: " + result.score());
        }
        if (result.justification() == null || result.justification().isBlank()) {
            throw new LlmResponseParseException("Scoring response was missing a justification");
        }

        return result;
    }

    private String buildUserPrompt(String resumeText, ExtractedResumeData extracted, String jobDescriptionText) {
        String skillsSummary = extracted.skills().isEmpty()
                ? "(none extracted)"
                : String.join(", ", extracted.skills());
        String experienceSummary = extracted.experience().isEmpty()
                ? "(none extracted)"
                : String.join("; ", extracted.experience());

        return """
                Compare the following résumé with this job description and rate the fit on a
                scale of 1 to 10, with justification.

                Return ONLY valid JSON with exactly this shape:
                {
                  "score": <integer 1-10>,
                  "justification": "<2-4 sentence explanation citing specific matches or gaps>"
                }

                Extracted skills: %s
                Extracted experience: %s

                Full résumé text:
                ---
                %s
                ---

                Job description:
                ---
                %s
                ---
                """.formatted(skillsSummary, experienceSummary, resumeText, jobDescriptionText);
    }
}
