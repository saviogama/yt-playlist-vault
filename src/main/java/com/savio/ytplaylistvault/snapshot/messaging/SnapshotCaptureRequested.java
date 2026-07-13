package com.savio.ytplaylistvault.snapshot.messaging;

import java.util.UUID;

public record SnapshotCaptureRequested(UUID monitoredPlaylistId) {}
