# YouTube Playlist Vault

> A silent observer of your YouTube/YT Music playlists. Tracking what changed, when it changed, and (whenever possible) why.

## The problem

Streaming playlists are alive: tracks disappear due to copyright takedowns, channels get deleted, videos become region-locked, or you accidentally remove something yourself. The issue is that **no platform keeps that history for you**. Once a track is gone, it's gone.

If you've ever had that "wait, wasn't there a song here?" feeling, this project exists to solve exactly that.

## The idea

**YT Playlist Vault** takes periodic "snapshots" of your YouTube playlists and builds a history of changes over time. With that, it can:

- **Capture the state of a playlist** at regular intervals (e.g. weekly), with no manual effort required.
- **Detect diffs** between two consecutive snapshots (what was added, removed, or moved).
- **Investigate why something disappeared**. When a track vanishes, the system queries the video's status via the API and tries to identify the cause (private, removed by owner, copyright claim, region restriction).
- **Tell the story of a playlist**. A browsable timeline of everything that has happened to it since you started monitoring it.

## How it works (architecture overview)

```
Google OAuth2 / YouTube Data API v3
                 |
                 v
            Spring Boot API <----> PostgreSQL
                 |
                 | scheduled capture requests
                 v
              RabbitMQ
                 |
                 v
     Snapshot consumer / Diff engine
```

### Main flow

1. The user authenticates via OAuth2 (Google) and grants read access to their playlists.
2. The user selects which playlists to monitor.
3. A scheduled job (`@Scheduled`) runs periodically and publishes one capture request for each active monitored playlist to RabbitMQ.
4. A RabbitMQ consumer refreshes the user's OAuth authorization when necessary, fetches the playlist state through `playlistItems.list`, and persists a snapshot only when content changed.
5. The current snapshot is compared against the most recent previous snapshot to generate a diff with added, removed, and moved items.
6. For each removed item, the system will query `videos.list` to try to understand why (private video, removed, blocked by region/copyright) and attach that information to the record (the "obituary").
7. The user can query the full timeline of any monitored playlist and see its complete evolution.

## Data model (high level)

| Entity              | Description                                                 |
| ------------------- | ----------------------------------------------------------- |
| `User`              | User authenticated via Google OAuth2                        |
| `MonitoredPlaylist` | A YouTube playlist the user has chosen to track             |
| `Snapshot`          | A snapshot of the playlist's state at a given point in time |
| `SnapshotItem`      | A track/video within a specific snapshot                    |
| `Diff`              | The result of comparing two consecutive snapshots           |
| `Obituary`          | A record explaining why a specific item disappeared         |

## Tech stack

- **Java 21 + Spring Boot** - application core
- **Spring Security + OAuth2 Client** - authentication via Google
- **Spring Data JPA + PostgreSQL** - persistence
- **Flyway** - versioned database migrations
- **Spring Scheduling** - periodic snapshot capture
- **Spring AMQP + RabbitMQ** - asynchronous scheduled capture processing, retries, and dead-letter queue
- **Spring Cache + Redis** - per-user cache for YouTube playlist discovery
- **YouTube Data API v3** - data source (free, no subscription required)
- **springdoc-openapi** - interactive API documentation with Swagger UI
- **Docker Compose** - local PostgreSQL and RabbitMQ environment

## Roadmap

- [x] Entity modeling and relationships
- [x] OAuth2 integration with Google + YouTube scope
- [x] Playlist discovery and monitored-playlist management
- [x] Snapshot capture service (API call + persistence)
- [x] Diff engine between snapshots, including item order changes
- [x] Snapshot timeline, details, and latest-diff REST endpoints
- [x] Scheduled asynchronous captures with RabbitMQ, retries, and dead-letter queue
- [x] Redis cache for YouTube playlist discovery
- [x] OpenAPI / Swagger UI documentation
- [ ] "Obituary" service (video removal status lookup)
- [ ] Simple dashboard to visualize the timeline

## Project status

Under active development. The backend supports Google OAuth2, playlist selection, monitored snapshots, diffs, scheduled asynchronous capture, and OpenAPI documentation. Remaining work is focused on removal investigation and a dashboard.
