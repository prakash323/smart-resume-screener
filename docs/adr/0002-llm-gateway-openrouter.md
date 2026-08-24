# 0002. LLM access via OpenRouter, with per-request model selection

## Status
Accepted

## Context
The assignment requires using an LLM for semantic extraction and match scoring, but does
not mandate a specific provider. The project owner holds an OpenRouter API key and
explicitly wants to try several different underlying models (OpenAI, Anthropic, Meta,
Google, etc.) without changing code each time - OpenRouter's value is being a single
OpenAI-compatible gateway in front of many providers, addressed by one API key and a
`provider/model` slug per request (e.g. `openai/gpt-4o-mini`, `anthropic/claude-sonnet-5`).

## Decision
- Define a small provider-agnostic `LlmClient` interface (`complete(system, user, model)`)
  in `llm/`, and implement it once against OpenRouter's `/chat/completions` endpoint using
  the JDK's built-in `java.net.http.HttpClient` - no HTTP client library dependency added.
- The model slug is a parameter on every extraction/scoring call, not a compile-time
  constant. `OPENROUTER_DEFAULT_MODEL` sets the fallback; both the API (`ScreeningRequest.model`,
  the `model` upload param) and the frontend's model selector let a caller override it
  per request.
- `GET /api/models` returns a small curated list of suggested slugs for the frontend
  dropdown, but the dropdown also accepts a free-text slug, so any model available on
  OpenRouter can be tried without redeploying.

## Consequences
- Swapping models to compare extraction/scoring quality is a UI action, not a code change -
  directly supports "test with lots of models."
- Because different models vary in how strictly they honor "return only JSON," the
  extraction and scoring services never trust raw output directly; they run it through
  `JsonExtractionUtil` (see ADR 0006) to pull out the JSON object regardless of any
  surrounding prose or markdown fences.
- The system only depends on OpenRouter's request/response envelope being OpenAI-compatible,
  not on any specific model's behavior - keeping the app portable to future models with zero
  code changes, only a slug string.
