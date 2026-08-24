# 0004. Résumé text extraction: Apache PDFBox

## Status
Accepted

## Context
Résumés arrive as PDF or plain text. We need to turn the PDF into plain text before
sending it to the LLM for extraction (LLMs are given text, not binary PDFs, to keep the
prompt cheap and provider-agnostic).

## Decision
Use Apache PDFBox (`org.apache.pdfbox:pdfbox`) for PDF text extraction via
`PDFTextStripper`. `.txt` uploads bypass PDF parsing entirely and are read as UTF-8 text.

PDFBox was chosen over alternatives (iText, Tika) because:
- It's a single, actively maintained, Apache-licensed library with no transitive
  dependency bloat for this use case.
- It requires no native binaries or external services (unlike OCR-based approaches),
  matching the "keep dependencies minimal and native" guideline.
- Apache Tika would pull in a much larger dependency tree (many format parsers we don't
  need) for the same PDF-to-text capability.

## Consequences
- Scanned/image-only PDFs (no embedded text layer) will fail extraction with a clear
  `ResumeParsingException` rather than silently returning empty text - OCR is out of scope.
- Encrypted/password-protected PDFs are explicitly rejected with a clear error rather than
  attempted.
- `ResumeParsingServiceTest` generates its own throwaway PDF fixture with PDFBox itself at
  test time instead of committing a binary PDF fixture to the repo, keeping the repo free of
  binary test assets and their diff/size overhead.
