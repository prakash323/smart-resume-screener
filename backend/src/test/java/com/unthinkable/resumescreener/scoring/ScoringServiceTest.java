package com.unthinkable.resumescreener.scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.dto.ExtractedResumeData;
import com.unthinkable.resumescreener.dto.LlmScoreResult;
import com.unthinkable.resumescreener.exception.LlmResponseParseException;
import com.unthinkable.resumescreener.llm.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScoringServiceTest {

    private LlmClient llmClient;
    private ScoringService scoringService;
    private final ExtractedResumeData extracted = new ExtractedResumeData(
            List.of("Java", "Spring Boot"), List.of("Backend Engineer at Acme"), List.of("B.Tech CS")
    );

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        OpenRouterProperties properties = new OpenRouterProperties();
        properties.setDefaultModel("openai/gpt-4o-mini");
        scoringService = new ScoringService(llmClient, new ObjectMapper(), properties);
    }

    @Test
    void parsesValidScoreAndJustification() {
        when(llmClient.complete(anyString(), anyString(), anyString()))
                .thenReturn("{\"score\":8,\"justification\":\"Strong Java and Spring Boot match.\"}");

        LlmScoreResult result = scoringService.score("resume text", extracted, "job description text", null);

        assertThat(result.score()).isEqualTo(8);
        assertThat(result.justification()).isEqualTo("Strong Java and Spring Boot match.");
    }

    @Test
    void throwsWhenScoreIsBelowValidRange() {
        when(llmClient.complete(anyString(), anyString(), anyString()))
                .thenReturn("{\"score\":0,\"justification\":\"no match\"}");

        assertThatThrownBy(() -> scoringService.score("resume text", extracted, "job description text", null))
                .isInstanceOf(LlmResponseParseException.class);
    }

    @Test
    void throwsWhenScoreIsAboveValidRange() {
        when(llmClient.complete(anyString(), anyString(), anyString()))
                .thenReturn("{\"score\":11,\"justification\":\"too good\"}");

        assertThatThrownBy(() -> scoringService.score("resume text", extracted, "job description text", null))
                .isInstanceOf(LlmResponseParseException.class);
    }

    @Test
    void throwsWhenJustificationIsMissing() {
        when(llmClient.complete(anyString(), anyString(), anyString()))
                .thenReturn("{\"score\":7}");

        assertThatThrownBy(() -> scoringService.score("resume text", extracted, "job description text", null))
                .isInstanceOf(LlmResponseParseException.class);
    }

    @Test
    void throwsWhenJustificationIsBlank() {
        when(llmClient.complete(anyString(), anyString(), anyString()))
                .thenReturn("{\"score\":7,\"justification\":\"   \"}");

        assertThatThrownBy(() -> scoringService.score("resume text", extracted, "job description text", null))
                .isInstanceOf(LlmResponseParseException.class);
    }
}
