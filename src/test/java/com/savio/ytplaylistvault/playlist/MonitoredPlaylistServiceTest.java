package com.savio.ytplaylistvault.playlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.dto.UpdateMonitoringStatusRequest;
import com.savio.ytplaylistvault.user.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MonitoredPlaylistServiceTest {

  private final MonitoredPlaylistRepository monitoredPlaylistRepository =
      Mockito.mock(MonitoredPlaylistRepository.class);
  private final MonitoredPlaylistService monitoredPlaylistService =
      new MonitoredPlaylistService(monitoredPlaylistRepository);

  @Test
  void pausesPlaylistForOwner() {
    User user = new User("google-123", "user@example.com", "User Example");
    MonitoredPlaylist playlist = playlist(user);

    when(monitoredPlaylistRepository.findByIdAndUser(playlist.getId(), user))
        .thenReturn(Optional.of(playlist));

    MonitoredPlaylist updatedPlaylist =
        monitoredPlaylistService.updateMonitoringStatus(
            user, playlist.getId(), new UpdateMonitoringStatusRequest(MonitoringStatus.PAUSED));

    assertThat(updatedPlaylist.getMonitoringStatus()).isEqualTo(MonitoringStatus.PAUSED);
  }

  @Test
  void rejectsStatusChangeForAnotherUser() {
    User owner = new User("google-owner", "owner@example.com", "Owner");
    User anotherUser = new User("google-other", "other@example.com", "Other User");
    UUID playlistId = UUID.randomUUID();

    when(monitoredPlaylistRepository.findByIdAndUser(playlistId, anotherUser))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                monitoredPlaylistService.updateMonitoringStatus(
                    anotherUser,
                    playlistId,
                    new UpdateMonitoringStatusRequest(MonitoringStatus.PAUSED)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Playlist not found");
  }

  private MonitoredPlaylist playlist(User user) {
    return new MonitoredPlaylist(
        user,
        "playlist-123",
        "Playlist",
        "Playlist used by MonitoredPlaylistServiceTest",
        "https://example.com/playlist.jpg");
  }
}
