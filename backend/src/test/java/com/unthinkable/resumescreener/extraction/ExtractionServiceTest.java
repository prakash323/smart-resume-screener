package com.unthinkable.resumescreener.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.dto.ExtractedResumeData;
import com.unthinkable.resumescreener.exception.LlmResponseParseException;
import com.unthinkable.resumescreener.llm.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtractionServiceTest {

    private LlmClient llmClient;
    private OpenRouterProperties properties;
    private ExtractionService extractionService;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        properties = new OpenRouterProperties();
        properties.setDefaultModel("openai/gpt-4o-mini");
        extractionService = new ExtractionService(llmClient, new ObjectMapper(), properties);
    }

    @Test
    void parsesWellFormedExtractionResponse() {
        when(llmClient.complete(anyString(), anyString(), eq("openai/gpt-4o-mini"))).thenReturn(
                "{\"skills\":[\"Java\",\"Spring Boot\"],\"experience\":[\"Backend Engineer at Acme (2021-2024)\"],\"education\":[\"B.Tech CS, XYZ University\"]}"
        );

        ExtractedResumeData result = extractionService.extract("some resume text", null);

        assertThat(result.skills()).containsExactly("Java", "Spring Boot");
        assertThat(result.experience()).containsExactly("Backend Engineer at Acme (2021-2024)");
        assertThat(result.education()).containsExactly("B.Tech CS, XYZ University");
    }

    @Test
    void fallsBackToDefaultModelWhenOverrideIsBlank() {
        when(llmClient.complete(anyString(), anyString(), eq("openai/gpt-4o-mini")))
                .thenReturn("{\"skills\":[],\"experience\":[],\"education\":[]}");

        extractionService.extract("resume text", "  ");

        // verifies the default model was used, not the blank override
        var captor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(llmClient).complete(anyString(), anyString(), captor.capture());
        assertThat(captor.getValue()).isEqualTo("openai/gpt-4o-mini");
    }

    @Test
    void usesModelOverrideWhenProvided() {
        when(llmClient.complete(anyString(), anyString(), eq("anthropic/claude-sonnet-5")))
                .thenReturn("{\"skills\":[],\"experience\":[],\"education\":[]}");

        extractionService.extract("resume text", "anthropic/claude-sonnet-5");

        org.mockito.Mockito.verify(llmClient).complete(anyString(), anyString(), eq("anthropic/claude-sonnet-5"));
    }

    @Test
    void treatsMissingFieldsAsEmptyLists() {
        when(llmClient.complete(anyString(), anyString(), anyString())).thenReturn("{\"skills\":[\"Python\"]}");

        ExtractedResumeData result = extractionService.extract("resume text", null);

        assertThat(result.skills()).containsExactly("Python");
        assertThat(result.experience()).isEmpty();
        assertThat(result.education()).isEmpty();
    }

    @Test
    void throwsWhenLlmReturnsMalformedJson() {
        when(llmClient.complete(anyString(), anyString(), anyString())).thenReturn("not json at all");

        assertThatThrownBy(() -> extractionService.extract("resume text", null))
                .isInstanceOf(LlmResponseParseException.class);
    }

    @Test
    void throwsWhenLlmReturnsJsonOfWrongShape() {
        when(llmClient.complete(anyString(), anyString(), anyString())).thenReturn("{\"skills\": \"Java\"}");

        assertThatThrownBy(() -> extractionService.extract("resume text", null))
                .isInstanceOf(LlmResponseParseException.class);
    }
}
