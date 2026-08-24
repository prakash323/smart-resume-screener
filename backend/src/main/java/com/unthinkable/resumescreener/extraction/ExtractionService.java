package com.unthinkable.resumescreener.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.dto.ExtractedResumeData;
import com.unthinkable.resumescreener.exception.LlmResponseParseException;
import com.unthinkable.resumescreener.llm.JsonExtractionUtil;
import com.unthinkable.resumescreener.llm.LlmClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Uses the LLM to turn raw resume text into structured skills / experience / education.
 * See ADR 0005 for why this is LLM-based rather than regex/NER.
 */
@Service
public class ExtractionService {

    static final String SYSTEM_PROMPT = """
            You are an expert résumé parser used inside an applicant tracking system.
            You read raw résumé text (which may contain messy PDF extraction artifacts,
            inconsistent spacing, or OCR noise) and return ONLY a single JSON object -
            no markdown, no code fences, no commentary before or after it.
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final OpenRouterProperties properties;

    public ExtractionService(LlmClient llmClient, ObjectMapper objectMapper, OpenRouterProperties properties) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public ExtractedResumeData extract(String resumeText, String modelOverride) {
        String model = (modelOverride == null || modelOverride.isBlank()) ? properties.getDefaultModel() : modelOverride;
        String userPrompt = buildUserPrompt(resumeText);

        String rawResponse = llmClient.complete(SYSTEM_PROMPT, userPrompt, model);
        String json = JsonExtractionUtil.extractJsonObject(rawResponse);

        try {
            return objectMapper.readValue(json, ExtractedResumeData.class);
        } catch (MismatchedInputException e) {
            throw new LlmResponseParseException("Extraction response did not match the expected schema: " + json, e);
        } catch (IOException e) {
            throw new LlmResponseParseException("Failed to parse extraction response as JSON: " + json, e);
        }
    }

    private String buildUserPrompt(String resumeText) {
        return """
                Extract structured data from the résumé below.

                Return ONLY valid JSON with exactly this shape:
                {
                  "skills": string[],       // technical & professional skills, deduplicated, no ratings
                  "experience": string[],   // one entry per role, e.g. "Senior Engineer at Acme Corp (2021-2024)"
                  "education": string[]     // one entry per qualification, e.g. "B.Tech Computer Science, XYZ University (2017-2021)"
                }

                If a section is not present in the résumé, return an empty array for it. Do not invent data.

                Résumé text:
                ---
                %s
                ---
                """.formatted(resumeText);
    }
}
