package com.unthinkable.resumescreener.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.exception.LlmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * LlmClient implementation backed by OpenRouter (https://openrouter.ai), an OpenAI-compatible
 * gateway that fronts many model providers behind a single API + API key. The model slug is
 * passed per-call so the same client can be pointed at different models for comparison.
 */
@Component
public class OpenRouterLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterLlmClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenRouterProperties properties;

    public OpenRouterLlmClient(HttpClient httpClient, ObjectMapper objectMapper, OpenRouterProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt, String model) {
        String requestBody = buildRequestBody(systemPrompt, userPrompt, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("HTTP-Referer", "https://github.com/unthinkable/resume-screener")
                .header("X-Title", "Smart Resume Screener")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new LlmException("Failed to reach OpenRouter for model " + model, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmException("OpenRouter call interrupted for model " + model, e);
        }

        if (response.statusCode() != 200) {
            log.warn("OpenRouter returned status {} for model {}: {}", response.statusCode(), model, response.body());
            throw new LlmException("OpenRouter returned status " + response.statusCode() + " for model " + model);
        }

        return extractContent(response.body(), model);
    }

    private String buildRequestBody(String systemPrompt, String userPrompt, String model) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("temperature", 0.2);

        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        return root.toString();
    }

    private String extractContent(String responseBody, String model) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new LlmException("OpenRouter response had no choices for model " + model + ": " + responseBody);
            }
            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new LlmException("OpenRouter response had empty content for model " + model);
            }
            return content.asText();
        } catch (IOException e) {
            throw new LlmException("Failed to parse OpenRouter response envelope for model " + model, e);
        }
    }
}
