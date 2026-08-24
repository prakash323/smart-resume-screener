# 0005. Structured extraction (skills/experience/education) is LLM-based, not regex/NER

## Status
Accepted

## Context
The assignment's evaluation criteria explicitly include "data extraction" and "LLM prompt
quality" - it expects the LLM itself to do the extraction work, not a secondary rule-based
system. Résumés are also famously unstructured and inconsistent in formatting, which makes
regex/keyword-list extraction brittle (e.g. a "Skills" section header spelled differently,
skills embedded in prose in the experience section, etc.).

## Decision
`ExtractionService` sends the full raw résumé text to the LLM with a system prompt that
fixes its role ("expert résumé parser... return ONLY a single JSON object") and a user
prompt that pins the exact output schema:

```json
{ "skills": string[], "experience": string[], "education": string[] }
```

The response is passed through `JsonExtractionUtil` (tolerant of code fences / leading
commentary) and then strictly deserialized into the `ExtractedResumeData` record. A
malformed or schema-mismatched response raises `LlmResponseParseException` rather than
silently returning partial/garbage data.

## Consequences
- Extraction quality is bounded by the chosen model's instruction-following ability, which
  is exactly why model selection is pluggable (ADR 0002) - a reviewer can compare
  extraction quality across models directly.
- No maintenance burden of a hand-written résumé grammar/keyword list that would need
  constant updates as résumé formats vary.
- Extraction failures are visible and typed (`LLM_RESPONSE_INVALID`, HTTP 502) rather than
  producing a resume record with silently empty/wrong fields - important for output clarity
  during grading.
