# Release Checklist

Use this checklist before publishing a GitHub release or tagging a public milestone.

## 1. Code and Quality Gates

- `./mvnw verify` passes locally
- `Spotless`, `Checkstyle`, `Surefire`, `Failsafe`, and `JaCoCo` checks are green
- no secrets or private environment details are included in the diff
- `README.md`, `CHANGELOG.md`, and any affected docs are updated

## 2. Integration Test Readiness

- the `integration-tests` GitHub environment exists
- required secrets are configured:
  - `BT_PANEL_BASE_URL`
  - `BT_PANEL_API_KEY`
- module-specific secrets are configured only when the corresponding suites are expected to run
- the `Integration Tests` workflow has been triggered manually and reviewed

## 3. Versioning and Release Notes

- `pom.xml` contains the intended version
- `CHANGELOG.md` describes the release accurately
- the Git tag name matches the release version, for example `v0.2.0`
- GitHub Release notes include upgrade notes and compatibility caveats

## 4. Publishing

Releases are cut from `master` and published by `.github/workflows/release.yml`:

1. Open a release commit that sets `pom.xml` to the release version (drop `-SNAPSHOT`) and renames
   the `[Unreleased]` changelog section to `[X.Y.Z] - YYYY-MM-DD`.
2. After it merges, tag that commit and push the tag:
   `git tag -a vX.Y.Z -m "vX.Y.Z" && git push origin vX.Y.Z`
3. The workflow checks that the tag matches the `pom.xml` version, runs `verify` with the
   `release` profile, and creates the GitHub Release with the jar, sources, and Javadoc attached.
   The release notes come from the matching `CHANGELOG.md` section.
4. Bump `pom.xml` to the next `-SNAPSHOT` version and add a fresh `[Unreleased]` section.

## 5. Repository Governance

- issue templates and the pull request template still reflect the current workflow
- `SECURITY.md` points to a valid private reporting channel
- the declared license still matches the repository contents and release metadata
