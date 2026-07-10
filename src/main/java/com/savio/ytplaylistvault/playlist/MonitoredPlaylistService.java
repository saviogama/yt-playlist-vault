package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.error.DuplicateResourceException;
import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.dto.CreateMonitoredPlaylistRequest;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoredPlaylistService {
  private final MonitoredPlaylistRepository monitoredPlaylistRepository;
  private final UserRepository userRepository;

  public MonitoredPlaylistService(
      MonitoredPlaylistRepository monitoredPlaylistRepository, UserRepository userRepository) {
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public MonitoredPlaylist createPlaylist(UUID userId, CreateMonitoredPlaylistRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (monitoredPlaylistRepository.existsByUserAndYoutubePlaylistId(
        user, request.youtubePlaylistId())) {
      throw new DuplicateResourceException("Playlist is already monitored by this user");
    }

    MonitoredPlaylist playlist =
        new MonitoredPlaylist(
            user,
            request.youtubePlaylistId(),
            request.title(),
            request.description(),
            request.thumbnailUrl());

    return monitoredPlaylistRepository.save(playlist);
  }

  @Transactional(readOnly = true)
  public List<MonitoredPlaylist> listPlaylists(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return monitoredPlaylistRepository.findByUser(user);
  }
}
