# 0006. Testing strategy: unit tests mock the LLM, integration tests mock the LLM bean, no live API calls in CI

## Status
Accepted

## Context
This app's core logic depends on a third-party LLM call that costs money, is
non-deterministic, and requires a live API key. A test suite that calls the real OpenRouter
API would be slow, flaky, non-reproducible, and unusable in CI (no key configured) or by a
grader who doesn't have the project owner's API key.

## Decision
Two layers of tests, neither of which calls a real LLM:

1. **Unit tests** (`ExtractionServiceTest`, `ScoringServiceTest`) mock the `LlmClient`
   interface with Mockito and feed it canned JSON responses, including deliberately
   malformed ones, to verify parsing, validation (score range, non-blank justification),
   and default-model fallback behavior.
2. **Integration tests** (`*ControllerIntegrationTest`) boot the full Spring context with
   `@SpringBootTest` + `MockMvc` against the H2 in-memory test profile, and replace the
   `LlmClient` bean with `@MockBean` - so the full upload → parse → extract → persist →
   score → shortlist flow is exercised end-to-end except for the actual network call.
3. `OpenRouterLlmClientTest` is the one place that *does* test real HTTP behavior - against
   a stub server built with the JDK's built-in `com.sun.net.httpserver.HttpServer`, so
   request formatting (model slug, message roles) and response-envelope parsing are verified
   without adding a mocking-HTTP-server dependency (e.g. WireMock) to the project.
4. `JsonExtractionUtilTest` is a focused table of edge cases (markdown fences, leading
   prose, nested braces inside string values, truncated/unterminated JSON) since this is the
   single point where model output unpredictability meets strict parsing.

## Consequences
- The full test suite runs deterministically and offline; `mvn test` never requires
  `OPENROUTER_API_KEY` to be set to a real key.
- `ScreeningControllerIntegrationTest`'s shortlist-ordering assertions double as a
  regression test for the "display shortlisted candidates" requirement, not just plumbing.
- Manual/exploratory testing against a real model is still expected before demoing (there is
  no substitute for seeing real model output quality), but that's a `curl`/UI smoke test, not
  part of the automated suite.
