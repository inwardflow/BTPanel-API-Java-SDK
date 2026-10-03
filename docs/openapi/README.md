# OpenAPI Workspace

This directory mixes two kinds of files:

- Canonical documentation artifacts that are useful to keep in the repository.
- Local validation outputs and capture files that are helpful while developing but should usually stay out of commits.

## Intended Repository Files

These files are the stable artifacts we expect to curate and reference from project documentation:

- `btpanel-developer-api.openapi-3.1.json`
- `btpanel-developer-api.strict.openapi-3.1.json`
- `btpanel-developer-api.observed.openapi-3.1.json`
- `live-validation-matrix.md`
- Supporting build and validation scripts such as `build-bt-openapi.mjs`

## Local-Only Working Files

These files are generated during live validation or browser-capture workflows and are ignored by default:

- `live-validation-report.json`
- Timestamped SSL validation reports
- DevTools capture validation outputs
- Backup files such as `*.bak*`

If a generated validation report becomes documentation-worthy, promote it into a curated markdown note instead of committing the raw working artifact.
