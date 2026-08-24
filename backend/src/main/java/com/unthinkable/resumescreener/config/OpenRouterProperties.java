package com.unthinkable.resumescreener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterProperties {

    /** API key for OpenRouter (https://openrouter.ai). Read from OPENROUTER_API_KEY env var. */
    private String apiKey;

    /** Base URL of the OpenRouter API, without a trailing slash. */
    private String baseUrl = "https://openrouter.ai/api/v1";

    /** Default model slug used when a request does not specify one, e.g. "openai/gpt-4o-mini". */
    private String defaultModel = "openai/gpt-4o-mini";

    private int timeoutSeconds = 60;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
