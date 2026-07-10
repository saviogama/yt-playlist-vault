package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotItemRequest;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotService {
  private final SnapshotRepository snapshotRepository;
  private final SnapshotItemRepository snapshotItemRepository;
  private final MonitoredPlaylistRepository monitoredPlaylistRepository;

  public SnapshotService(
      SnapshotRepository snapshotRepository,
      SnapshotItemRepository snapshotItemRepository,
      MonitoredPlaylistRepository monitoredPlaylistRepository) {
    this.snapshotRepository = snapshotRepository;
    this.snapshotItemRepository = snapshotItemRepository;
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
  }

  private MonitoredPlaylist getPlaylistOrThrow(UUID playlistId) {
    return monitoredPlaylistRepository
        .findById(playlistId)
        .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));
  }

  @Transactional
  public SnapshotWithItems createSnapshot(UUID playlistId, CreateSnapshotRequest request) {
    MonitoredPlaylist playlist = getPlaylistOrThrow(playlistId);

    Snapshot snapshot = new Snapshot(playlist, Instant.now(), request.items().size());

    Snapshot savedSnapshot = snapshotRepository.save(snapshot);

    List<SnapshotItem> items =
        request.items().stream()
            .map(itemRequest -> createSnapshotItem(savedSnapshot, itemRequest))
            .toList();

    List<SnapshotItem> savedItems = snapshotItemRepository.saveAll(items);

    return new SnapshotWithItems(savedSnapshot, savedItems);
  }

  @Transactional(readOnly = true)
  public List<SnapshotWithItems> listSnapshots(UUID playlistId) {
    MonitoredPlaylist playlist = getPlaylistOrThrow(playlistId);

    return snapshotRepository.findByMonitoredPlaylistOrderByCapturedAtDesc(playlist).stream()
        .map(
            snapshot ->
                new SnapshotWithItems(
                    snapshot, snapshotItemRepository.findBySnapshotOrderByPositionAsc(snapshot)))
        .toList();
  }

  @Transactional(readOnly = true)
  public SnapshotWithItems getSnapshot(UUID snapshotId) {
    Snapshot snapshot =
        snapshotRepository
            .findById(snapshotId)
            .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found"));

    List<SnapshotItem> items = snapshotItemRepository.findBySnapshotOrderByPositionAsc(snapshot);

    return new SnapshotWithItems(snapshot, items);
  }

  private SnapshotItem createSnapshotItem(Snapshot snapshot, CreateSnapshotItemRequest request) {
    return new SnapshotItem(
        snapshot,
        request.youtubeVideoId(),
        request.title(),
        request.channelTitle(),
        request.thumbnailUrl(),
        request.position(),
        request.addedToPlaylistAt());
  }

  public record SnapshotWithItems(Snapshot snapshot, List<SnapshotItem> items) {}
}
