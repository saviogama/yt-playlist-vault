# ADR 0006: Scheduled Captures and OAuth Token Storage

- **Status**: Accepted
- **Date**: 2026-07-13

## Context

Manual snapshot capture runs inside an authenticated HTTP request and can use the current user's OAuth authorization. Scheduled execution runs without a browser session, but still requires the user's YouTube authorization.

## Decision

Persist OAuth authorized clients in PostgreSQL using Spring Security's `JdbcOAuth2AuthorizedClientService` and its standard `oauth2_authorized_client` table.

Request Google authorization with `access_type=offline` so that a refresh token can be returned. Configure an `OAuth2AuthorizedClientManager` with refresh-token support so expired access tokens are refreshed before scheduled captures.

Enable Spring Scheduling. Run the snapshot job every 72 hours after the previous execution completes. The job processes only `ACTIVE` monitored playlists, groups them by user, and obtains one OAuth authorization per user for each run.

When authorization is unavailable or a playlist capture fails, log the failure and continue processing the remaining playlists. Detailed external-integration error tracking is deferred to a later feature.

## Consequences

Scheduled captures survive application restarts and do not depend on an open browser session.

Users must reauthorize once after this migration so their authorization is stored in the database. Refresh tokens are sensitive credentials; production deployment must protect the database and use encryption at rest.

The fixed-delay schedule prevents overlapping executions of this job in one application instance. A distributed lock will be needed before running multiple application instances.
