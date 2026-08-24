# 0007. Frontend: React + Vite, no UI library, no HTTP/router dependency

## Status
Accepted

## Context
The assignment lists a frontend dashboard as optional; the project owner asked for a full
React dashboard. The submission guidelines still ask to "keep dependencies minimal and
native whenever possible," so the goal was a real, usable dashboard without importing a
component library, a router, or an HTTP client for what's a small number of screens/calls.

## Decision
- Vite + React 18, scaffolded manually rather than via `create-react-app` (unmaintained) or
  a template that bundles extra tooling.
- No UI kit (MUI, Chakra, etc.) - hand-written CSS (`App.css`) covers the layout, form
  styling, and score-tier badges.
- No `axios` - the native `fetch` API is sufficient for this app's request shapes
  (`src/api/client.js`), including multipart upload via `FormData`.
- No `react-router` - the whole app is a single view with local component state
  (upload panel, job-description panel, shortlist panel); there's nothing to route between.
- Testing uses Vitest (Vite-native, zero extra config) + React Testing Library, not Jest,
  since Vitest reuses the same Vite transform pipeline the app already needs.

## Consequences
- `package.json` has exactly two runtime dependencies (`react`, `react-dom`) and five dev
  dependencies, all directly load-bearing for building or testing the app - nothing is in
  there "just in case."
- If the dashboard grows to multiple real pages/routes later, `react-router` becomes
  justified then - this decision applies to the current, single-view scope, not as a
  permanent constraint.
