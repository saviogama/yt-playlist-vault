# ADR 0005: Code Formatting and Quality Tools

- **Status**: Accepted
- **Date**: 2026-07-10

## Context

The project should keep Java code formatting consistent across contributors and branches.

Manual formatting decisions create noise in reviews and make diffs harder to read. The project needs a simple automated formatter before adding stricter static analysis tools.

## Decision

Use Spotless with google-java-format for Java code formatting.

The project should support:

```powershell
.\mvnw.cmd spotless:apply
```

to format code, and:

```powershell
.\mvnw.cmd spotless:check
```

to verify formatting without changing files.

For now, do not add stricter lint or static analysis tools such as Checkstyle, PMD, SpotBugs, or Error Prone.

Those tools may be evaluated later when the codebase has more business logic and clearer quality rules.

## Consequences

Formatting becomes automated and consistent.

Code reviews should focus less on style and more on behavior, design, tests, and correctness.

The formatter may change existing code layout, so the first formatting commit may touch many files. After that, future diffs should become cleaner.

Spotless is a build tool, not an application dependency. It belongs in the Maven plugins section, not in application dependencies.
