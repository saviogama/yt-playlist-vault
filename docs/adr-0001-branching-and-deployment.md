# ADR 0001: Branching and Deployment Workflow

- **Status**: Accepted
- **Date**: 2026-07-05

## Context

The project should keep a simple branch strategy that supports review, integration, and production deployment without adding unnecessary release process overhead.

## Decision

Use a three-level branch workflow:

- `main`: production branch. Only reviewed and verified changes should land here.
- `dev`: integration branch. Feature branches merge here first.
- `feature/*`: short-lived branches for focused changes.

The normal flow is:

1. Create `feature/*` from `dev`.
2. Commit focused changes using Conventional Commits.
3. Open or perform a merge from `feature/*` into `dev`.
4. Promote `dev` into `main` when the integrated state is ready for production.

Production deploys should happen only from `main`.

The project should use `main` as the Production Branch. Pull requests and non-production branches may still produce preview deployments, but they should not be considered production releases.

CI should run for pushes and pull requests before code is promoted. The baseline checks are:

- Lint
- Format check
- Unit tests
- End-to-end tests
- Production build

## Commit Standard

Use Conventional Commits:

- `feat:` for user-facing functionality
- `fix:` for bug fixes
- `refactor:` for internal structure changes without behavior changes
- `test:` for automated tests
- `docs:` for documentation
- `ci:` for CI/CD configuration
- `chore:` for tooling and maintenance

Commits should stay focused by concern. Avoid bundling unrelated feature, test, and CI changes into the same commit.

## Consequences

This keeps `main` deployable, gives `dev` a place to absorb feature work, and keeps the MVP history readable.
