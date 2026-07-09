# ADR 0003: API DTOs and HTTP Contracts

- **Status**: Accepted
- **Date**: 2026-07-09

## Context

The REST API should not expose JPA entities directly. Entities represent persistence and domain state, while HTTP payloads are public contracts that may evolve differently.

## Decision

Use DTO records for request and response payloads.

Name request DTOs by use case:

- `CreateUserRequest`
- `CreateMonitoredPlaylistRequest`
- `UpdateUserRequest`

Name response DTOs by the resource representation:

- `UserResponse`
- `MonitoredPlaylistResponse`

Keep DTOs inside a feature-local `dto` package:

```text
playlist/
  dto/
    CreateMonitoredPlaylistRequest.java
    MonitoredPlaylistResponse.java
```

Request DTOs should use Jakarta Bean Validation annotations for input validation, such as:

- `@NotBlank`
- `@Email`
- `@Valid`

Response DTOs may expose static factory methods such as `from(entity)` to map internal entities into HTTP response shapes.

Controllers should receive request DTOs, call services, and return response DTOs.

## Consequences

The API contract stays decoupled from persistence details.

DTO names remain explicit as endpoints grow. This avoids generic request types with unclear semantics or many optional fields.

Mapping code adds some repetition, but it keeps entity design and HTTP representation from becoming accidentally coupled.

