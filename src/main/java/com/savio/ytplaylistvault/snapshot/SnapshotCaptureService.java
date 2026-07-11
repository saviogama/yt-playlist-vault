package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotWithItems;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotItemRequest;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.youtube.YoutubePlaylistService;
import com.savio.ytplaylistvault.youtube.dto.YoutubePlaylistItemResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotCaptureService {
  private final MonitoredPlaylistRepository monitoredPlaylistRepository;
  private final YoutubePlaylistService youtubePlaylistService;
  private final SnapshotService snapshotService;

  public SnapshotCaptureService(
      MonitoredPlaylistRepository monitoredPlaylistRepository,
      YoutubePlaylistService youtubePlaylistService,
      SnapshotService snapshotService) {
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
    this.youtubePlaylistService = youtubePlaylistService;
    this.snapshotService = snapshotService;
  }

  @Transactional
  public SnapshotWithItems captureSnapshot(User user, UUID playlistId, String accessToken) {
    MonitoredPlaylist playlist =
        monitoredPlaylistRepository
            .findByIdAndUser(playlistId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

    List<YoutubePlaylistItemResponse> youtubeItems =
        youtubePlaylistService.listPlaylistItems(accessToken, playlist.getProviderPlaylistId());

    CreateSnapshotRequest request =
        new CreateSnapshotRequest(
            youtubeItems.stream().map(this::toCreateSnapshotItemRequest).toList());

    return snapshotService.createSnapshotForUser(user, playlistId, request);
  }

  private CreateSnapshotItemRequest toCreateSnapshotItemRequest(
      YoutubePlaylistItemResponse youtubeItem) {
    return new CreateSnapshotItemRequest(
        youtubeItem.providerItemId(),
        youtubeItem.title(),
        youtubeItem.creatorName(),
        youtubeItem.thumbnailUrl(),
        youtubeItem.position(),
        youtubeItem.addedToPlaylistAt());
  }
}
