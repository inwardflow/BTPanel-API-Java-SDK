# Contributing

Thank you for helping improve this SDK.

## Getting Started

1. Read [README.md](README.md) and [docs/architecture.md](docs/architecture.md) for the current public API direction and package layout.
2. Use the Maven Wrapper so local Maven installations do not affect reproducibility.
3. Before opening a pull request, run:

```bash
./mvnw verify
```

## Development Guidelines

- Prefer adding or updating unit tests alongside new functionality.
- Keep live BTPanel calls out of the default build. Real-panel integration tests must stay opt-in.
- As a library, the SDK should not force a concrete logging backend on consumers.
- If a public API changes, update the relevant documentation in `README.md`, `docs/`, and `CHANGELOG.md`.

## Pull Request Expectations

- Keep each pull request focused on a single change or tightly related set of changes.
- Use clear, action-oriented titles such as `refactor website facade naming` or `fix FTP response parsing`.
- In the description, explain:
  - what changed
  - why it changed
  - how it was validated

## Before Submitting

- Code is formatted and style checks pass.
- `./mvnw verify` passes locally.
- Public-facing documentation is updated when behavior or API contracts changed.
