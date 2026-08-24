package com.unthinkable.resumescreener.llm;

/**
 * Thin abstraction over a chat-completion LLM gateway. Kept provider-agnostic and
 * model-pluggable so callers can pick a model per request (see ADR 0002).
 */
public interface LlmClient {

    /**
     * Sends a single system+user turn to the given model and returns the raw text content
     * of the model's reply (expected to be a JSON document per this app's prompts).
     *
     * @param systemPrompt instructions establishing the assistant's role and output contract
     * @param userPrompt   the task-specific content (resume text, job description, etc.)
     * @param model        an OpenRouter model slug, e.g. "openai/gpt-4o-mini"
     */
    String complete(String systemPrompt, String userPrompt, String model);
}
