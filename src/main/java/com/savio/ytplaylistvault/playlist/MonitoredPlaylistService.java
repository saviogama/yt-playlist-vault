package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.error.DuplicateResourceException;
import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.dto.CreateMonitoredPlaylistRequest;
import com.savio.ytplaylistvault.playlist.dto.UpdateMonitoringStatusRequest;
import com.savio.ytplaylistvault.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoredPlaylistService {
  private final MonitoredPlaylistRepository monitoredPlaylistRepository;

  public MonitoredPlaylistService(MonitoredPlaylistRepository monitoredPlaylistRepository) {
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
  }

  @Transactional
  public MonitoredPlaylist createPlaylist(User user, CreateMonitoredPlaylistRequest request) {
    if (monitoredPlaylistRepository.existsByUserAndProviderPlaylistId(
        user, request.providerPlaylistId())) {
      throw new DuplicateResourceException("Playlist is already monitored by this user");
    }

    MonitoredPlaylist playlist =
        new MonitoredPlaylist(
            user,
            request.providerPlaylistId(),
            request.title(),
            request.description(),
            request.thumbnailUrl());

    return monitoredPlaylistRepository.save(playlist);
  }

  @Transactional(readOnly = true)
  public List<MonitoredPlaylist> listPlaylists(User user) {
    return monitoredPlaylistRepository.findByUser(user);
  }

  @Transactional
  public MonitoredPlaylist updateMonitoringStatus(
      User user, UUID playlistId, UpdateMonitoringStatusRequest request) {
    MonitoredPlaylist playlist =
        monitoredPlaylistRepository
            .findByIdAndUser(playlistId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

    playlist.changeMonitoringStatus(request.status());

    return playlist;
  }
}
