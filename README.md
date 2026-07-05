# YouTube Playlist Vault

> A silent observer of your YouTube/YT Music playlists. Tracking what changed, when it changed, and (whenever possible) why.

## The problem

Streaming playlists are alive: tracks disappear due to copyright takedowns, channels get deleted, videos become region-locked, or you accidentally remove something yourself. The issue is that **no platform keeps that history for you**. Once a track is gone, it's gone.

If you've ever had that "wait, wasn't there a song here?" feeling, this project exists to solve exactly that.

## The idea

**YT Playlist Vault** takes periodic "snapshots" of your YouTube playlists and builds a history of changes over time. With that, it can:

- **Capture the state of a playlist** at regular intervals (e.g. weekly), with no manual effort required.
- **Detect diffs** between two consecutive snapshot (what was added, what was removed).
- **Investigate why something disappeared**. When a track vanishes, the system queries the video's status via the API and tries to identify the cause (private, removed by owner, copyright claim, region restriction).
- **Tell the story of a playlist**. A browsable timeline of everything that has happened to it since you started monitoring it.

## How it works (architecture overview)

```
YouTube Data     ◄──┤ Spring Boot App │──►  PostgreSQL
API v3 (OAuth2)     │                 │
                    │- Scheduler      │
                    │- Snapshot Svc   │
                    │- Diff Engine    │
                    │- Obituary Svc   │
                    └─────────────────┘
                            │
                            ▼
                        REST API
                        Dashboard
```

### Main flow

1. The user authenticates via OAuth2 (Google) and grants read access to their playlists.
2. The user selects which playlists to monitor.
3. A scheduled job (`@Scheduled`) runs periodically and, for each monitored playlist, fetches its current state via `playlistItems.list`.
4. The current snapshot is compared against the most recent previous snapshot → generates a `Diff` record (items added, items removed).
5. For each removed item, the system queries `videos.list` to try to understand why (private video, removed, blocked by region/copyright) and attaches that information to the record (the "obituary").
6. The user can query the full timeline of any monitored playlist and see its complete evolution.

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
- **Spring Scheduling** - periodic snapshot capture
- **YouTube Data API v3** - data source (free, no subscription required)
- **Docker Compose** - local environment (app + database)

## Roadmap

- [ ] Entity modeling and relationships
- [ ] OAuth2 integration with Google + YouTube scope
- [ ] Snapshot capture service (API call + persistence)
- [ ] Diff engine between snapshots
- [ ] "Obituary" service (video removal status lookup)
- [ ] REST endpoint for per-playlist timeline
- [ ] Simple dashboard to visualize the timeline

## Project status

🚧 Under construction — this README documents the design phase.
