# ADR 0007: Asynchronous Snapshot Captures with RabbitMQ

- **Status**: Accepted
- **Date**: 2026-07-13

## Context

Scheduled snapshot capture previously performed OAuth authorization, YouTube API calls, snapshot comparison, and persistence in the scheduler thread.

This couples scheduling duration to external API latency and means one slow or failing capture can delay the remaining work. The project also needs to demonstrate asynchronous message processing and failure isolation.

## Decision

Use RabbitMQ to process scheduled snapshot captures asynchronously.

The scheduler selects `ACTIVE` monitored playlists and publishes one `SnapshotCaptureRequested` message per playlist. Each message contains only `monitoredPlaylistId`; OAuth access tokens are not sent through the broker.

Use a durable direct exchange and queue for capture requests:

- Exchange: `snapshot.capture.exchange`
- Routing key: `snapshot.capture.requested`
- Queue: `snapshot.capture.queue`

Use JSON message serialization.

A consumer receives the request and delegates processing to `SnapshotCaptureMessageProcessor`. The processor reloads the playlist and user from the database, skips stale or paused playlists, refreshes OAuth authorization when available, and invokes the existing snapshot capture service.

Configure three listener attempts with exponential backoff. Messages that continue failing are rejected without requeue and routed to a durable dead-letter queue:

- Dead-letter exchange: `snapshot.capture.dlx`
- Dead-letter routing key: `snapshot.capture.failed`
- Dead-letter queue: `snapshot.capture.dlq`

Manual snapshot capture remains synchronous through the HTTP endpoint.

## Consequences

Scheduled work is decoupled from external API latency and can be processed by consumers independently from the scheduler.

Message processing has at-least-once delivery semantics. A message may be redelivered after a consumer failure, so processing must remain idempotent. Existing unchanged-snapshot detection prevents duplicate snapshots when playlist content did not change.

Messages that cannot be processed are preserved in the DLQ for investigation instead of being lost or retried indefinitely.

The current scheduler-to-broker publication is not protected by a transactional outbox. If publication fails, the scheduler logs the failure and the playlist is retried on a future scheduled execution. An outbox can be introduced later if stronger delivery guarantees become necessary.
