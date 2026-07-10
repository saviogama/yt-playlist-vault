package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotWithItems;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SnapshotController {
  private final SnapshotService snapshotService;

  public SnapshotController(SnapshotService snapshotService) {
    this.snapshotService = snapshotService;
  }

  @PostMapping("/playlists/{playlistId}/snapshots")
  @ResponseStatus(HttpStatus.CREATED)
  public SnapshotResponse createSnapshot(
      @PathVariable UUID playlistId, @Valid @RequestBody CreateSnapshotRequest request) {
    SnapshotWithItems snapshotWithItems = snapshotService.createSnapshot(playlistId, request);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }

  @GetMapping("/playlists/{playlistId}/snapshots")
  public List<SnapshotResponse> listSnapshots(@PathVariable UUID playlistId) {
    return snapshotService.listSnapshots(playlistId).stream()
        .map(
            snapshotWithItems ->
                SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items()))
        .toList();
  }

  @GetMapping("/snapshots/{snapshotId}")
  public SnapshotResponse getSnapshot(@PathVariable UUID snapshotId) {
    SnapshotWithItems snapshotWithItems = snapshotService.getSnapshot(snapshotId);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }
}
