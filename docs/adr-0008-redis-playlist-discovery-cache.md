# ADR 0008: Redis Cache for YouTube Playlist Discovery

- **Status**: Accepted
- **Date**: 2026-07-13

## Context

Listing the authenticated user's YouTube playlists calls the external YouTube Data API. Repeated requests from the same user return data that changes infrequently, consume API quota, and add unnecessary latency.

Snapshot capture must not reuse cached playlist-item data because it needs the current remote state to detect additions, removals, and order changes.

## Decision

Use Spring Cache backed by Redis for `GET /api/youtube/playlists` only.

Cache entries use the authenticated user's Google subject as the key. Access tokens are deliberately excluded because they are sensitive credentials and can change after refresh.

Configure a five-minute TTL, JSON serialization, and disabled null-value caching. Redis repository support remains disabled because Redis is used only as a cache, not as a persistence store.

## Consequences

Repeated playlist-discovery requests by the same user within five minutes avoid a YouTube API call. Different users have isolated cache entries.

Playlist discovery can be stale for at most five minutes. Snapshot capture remains uncached and therefore continues to detect the latest playlist state.

Redis is an optional runtime dependency for local development through Docker Compose. Tests disable the Redis cache and use an in-memory cache for focused cache behavior verification.
