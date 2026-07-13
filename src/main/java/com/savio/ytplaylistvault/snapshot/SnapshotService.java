package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotItemRequest;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffItemResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotMovedItemResponse;
import com.savio.ytplaylistvault.user.User;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotService {
  private final SnapshotRepository snapshotRepository;
  private final SnapshotItemRepository snapshotItemRepository;
  private final MonitoredPlaylistRepository monitoredPlaylistRepository;
  private final Clock clock;

  public SnapshotService(
      SnapshotRepository snapshotRepository,
      SnapshotItemRepository snapshotItemRepository,
      MonitoredPlaylistRepository monitoredPlaylistRepository,
      Clock clock) {
    this.snapshotRepository = snapshotRepository;
    this.snapshotItemRepository = snapshotItemRepository;
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
    this.clock = clock;
  }

  private Snapshot getSnapshotForUserOrThrow(UUID snapshotId, User user) {
    return snapshotRepository
        .findById(snapshotId)
        .filter(snapshot -> snapshot.getMonitoredPlaylist().getUser().getId().equals(user.getId()))
        .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found"));
  }

  private MonitoredPlaylist getPlaylistOrThrow(UUID playlistId) {
    return monitoredPlaylistRepository
        .findById(playlistId)
        .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));
  }

  private MonitoredPlaylist getPlaylistForUserOrThrow(UUID playlistId, User user) {
    return monitoredPlaylistRepository
        .findByIdAndUser(playlistId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));
  }

  @Transactional
  public SnapshotWithItems createSnapshotForUser(
      User user, UUID playlistId, CreateSnapshotRequest request) {
    MonitoredPlaylist playlist = getPlaylistForUserOrThrow(playlistId, user);

    return createSnapshot(playlist, request);
  }

  @Transactional
  public SnapshotWithItems createSnapshot(UUID playlistId, CreateSnapshotRequest request) {
    MonitoredPlaylist playlist = getPlaylistOrThrow(playlistId);
    return createSnapshot(playlist, request);
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

  @Transactional(readOnly = true)
  public SnapshotDiffResponse diffPreviousSnapshot(UUID snapshotId) {
    Snapshot currentSnapshot =
        snapshotRepository
            .findById(snapshotId)
            .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found"));

    Snapshot previousSnapshot =
        snapshotRepository
            .findFirstByMonitoredPlaylistAndCapturedAtBeforeOrderByCapturedAtDesc(
                currentSnapshot.getMonitoredPlaylist(), currentSnapshot.getCapturedAt())
            .orElse(null);

    if (previousSnapshot == null) {
      return new SnapshotDiffResponse(
          currentSnapshot.getId(), null, List.of(), List.of(), List.of());
    }

    List<SnapshotItem> currentItems =
        snapshotItemRepository.findBySnapshotOrderByPositionAsc(currentSnapshot);
    List<SnapshotItem> previousItems =
        snapshotItemRepository.findBySnapshotOrderByPositionAsc(previousSnapshot);

    Map<String, SnapshotItem> currentItemsByProviderItemId = indexByProviderItemId(currentItems);
    Map<String, SnapshotItem> previousItemsByProviderItemId = indexByProviderItemId(previousItems);

    List<SnapshotDiffItemResponse> addedItems =
        currentItems.stream()
            .filter(item -> !previousItemsByProviderItemId.containsKey(item.getProviderItemId()))
            .map(this::toDiffItemResponse)
            .toList();

    List<SnapshotDiffItemResponse> removedItems =
        previousItems.stream()
            .filter(item -> !currentItemsByProviderItemId.containsKey(item.getProviderItemId()))
            .map(this::toDiffItemResponse)
            .toList();

    List<SnapshotMovedItemResponse> movedItems =
        currentItems.stream()
            .filter(item -> previousItemsByProviderItemId.containsKey(item.getProviderItemId()))
            .filter(
                item ->
                    previousItemsByProviderItemId.get(item.getProviderItemId()).getPosition()
                        != item.getPosition())
            .map(
                item ->
                    toMovedItemResponse(
                        item, previousItemsByProviderItemId.get(item.getProviderItemId())))
            .toList();

    return new SnapshotDiffResponse(
        currentSnapshot.getId(), previousSnapshot.getId(), addedItems, removedItems, movedItems);
  }

  @Transactional(readOnly = true)
  public List<SnapshotWithItems> listSnapshotsForUser(User user, UUID playlistId) {
    MonitoredPlaylist playlist = getPlaylistForUserOrThrow(playlistId, user);

    return snapshotRepository.findByMonitoredPlaylistOrderByCapturedAtDesc(playlist).stream()
        .map(
            snapshot ->
                new SnapshotWithItems(
                    snapshot, snapshotItemRepository.findBySnapshotOrderByPositionAsc(snapshot)))
        .toList();
  }

  @Transactional(readOnly = true)
  public SnapshotWithItems getSnapshotForUser(User user, UUID snapshotId) {
    Snapshot snapshot = getSnapshotForUserOrThrow(snapshotId, user);

    List<SnapshotItem> items = snapshotItemRepository.findBySnapshotOrderByPositionAsc(snapshot);

    return new SnapshotWithItems(snapshot, items);
  }

  @Transactional(readOnly = true)
  public SnapshotDiffResponse diffPreviousSnapshotForUser(User user, UUID snapshotId) {
    getSnapshotForUserOrThrow(snapshotId, user);

    return diffPreviousSnapshot(snapshotId);
  }

  private SnapshotItem createSnapshotItem(Snapshot snapshot, CreateSnapshotItemRequest request) {
    return new SnapshotItem(
        snapshot,
        request.providerItemId(),
        request.title(),
        request.creatorName(),
        request.thumbnailUrl(),
        request.position(),
        request.addedToPlaylistAt());
  }

  private Map<String, SnapshotItem> indexByProviderItemId(List<SnapshotItem> items) {
    return items.stream()
        .collect(Collectors.toMap(SnapshotItem::getProviderItemId, Function.identity()));
  }

  private SnapshotDiffItemResponse toDiffItemResponse(SnapshotItem item) {
    return new SnapshotDiffItemResponse(
        item.getProviderItemId(),
        item.getTitle(),
        item.getCreatorName(),
        item.getThumbnailUrl(),
        item.getPosition());
  }

  private SnapshotMovedItemResponse toMovedItemResponse(
      SnapshotItem currentItem, SnapshotItem previousItem) {
    return new SnapshotMovedItemResponse(
        currentItem.getProviderItemId(),
        currentItem.getTitle(),
        currentItem.getCreatorName(),
        currentItem.getThumbnailUrl(),
        previousItem.getPosition(),
        currentItem.getPosition());
  }

  private SnapshotWithItems createSnapshot(
      MonitoredPlaylist playlist, CreateSnapshotRequest request) {
    Snapshot snapshot = new Snapshot(playlist, Instant.now(clock), request.items().size());

    Snapshot savedSnapshot = snapshotRepository.save(snapshot);

    List<SnapshotItem> items =
        request.items().stream()
            .map(itemRequest -> createSnapshotItem(savedSnapshot, itemRequest))
            .toList();

    List<SnapshotItem> savedItems = snapshotItemRepository.saveAll(items);

    return new SnapshotWithItems(savedSnapshot, savedItems);
  }

  public record SnapshotWithItems(Snapshot snapshot, List<SnapshotItem> items) {}
}
