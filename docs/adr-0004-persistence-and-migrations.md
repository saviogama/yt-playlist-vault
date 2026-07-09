# ADR 0004: Persistence and Database Migrations

- **Status**: Accepted
- **Date**: 2026-07-09

## Context

The project uses PostgreSQL with Spring Data JPA and Hibernate. Database schema changes should be explicit, reviewable, and reproducible across environments.

## Decision

Use Flyway for database migrations.

Use Hibernate schema validation instead of automatic schema creation or mutation:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

The expected flow is:

1. Create or change a JPA entity.
2. Add a matching Flyway migration under `src/main/resources/db/migration`.
3. Run tests or start the application.
4. Flyway applies migrations.
5. Hibernate validates that entities match the database schema.

Migration files should follow Flyway naming:

```text
V1__create_users_table.sql
V2__create_monitored_playlists_table.sql
```

Tables use plural snake_case names, while Java entity classes use singular PascalCase names:

```text
User -> users
MonitoredPlaylist -> monitored_playlists
SnapshotItem -> snapshot_items
```

Important constraints should be named explicitly using clear prefixes:

- `pk_` for primary keys when declared explicitly
- `fk_` for foreign keys
- `uk_` for unique constraints
- `ck_` for check constraints

## Consequences

Schema evolution is controlled by versioned SQL instead of Hibernate auto-updates.

The application fails fast when entity mappings and database schema diverge.

Writing migrations manually creates some duplication with entity classes, but it gives better control over constraints, indexes, data safety, and deployment behavior.

