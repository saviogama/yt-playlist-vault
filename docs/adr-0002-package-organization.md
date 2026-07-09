# ADR 0002: Package Organization

- **Status**: Accepted
- **Date**: 2026-07-09

## Context

The application is organized around domain areas such as users, monitored playlists, and snapshots. As the codebase grows, keeping related files close together should make changes easier to navigate and review.

## Decision

Organize application code primarily by feature/domain area instead of global technical layers.

Use package roots such as:

- `user`
- `playlist`
- `snapshot`

Within each feature package, keep the main application components close to the domain concept:

- entity
- repository
- service
- controller

Use feature-local subpackages when a role is expected to grow. DTOs should live under a `dto` subpackage inside the feature package:

```text
user/
  dto/
    CreateUserRequest.java
    UserResponse.java
  User.java
  UserRepository.java
  UserService.java
  UserController.java
```

Avoid global packages such as `controller`, `service`, `repository`, and `dto` at the application root unless a future cross-cutting concern clearly requires them.

## Consequences

Feature work stays localized, making it easier to inspect everything related to one domain area.

Packages may contain a mix of technical roles, but this is intentional. The feature boundary is more important than grouping all classes of the same technical type together.

