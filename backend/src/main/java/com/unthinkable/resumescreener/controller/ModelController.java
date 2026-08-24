package com.unthinkable.resumescreener.controller;

import com.unthinkable.resumescreener.config.OpenRouterProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Exposes a curated list of OpenRouter model slugs for the frontend model picker, plus the
 * server's configured default. The picker also accepts free-text model slugs so any model
 * available on OpenRouter can be tried without a code change.
 */
@RestController
@RequestMapping("/api/models")
public class ModelController {

    private static final List<String> SUGGESTED_MODELS = List.of(
            "openai/gpt-4o-mini",
            "openai/gpt-4o",
            "anthropic/claude-sonnet-5",
            "anthropic/claude-haiku-4.5",
            "meta-llama/llama-3.1-70b-instruct",
            "google/gemini-3.5-flash"
    );

    private final OpenRouterProperties properties;

    public ModelController(OpenRouterProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public Map<String, Object> listModels() {
        return Map.of(
                "defaultModel", properties.getDefaultModel(),
                "suggested", SUGGESTED_MODELS
        );
    }
}
