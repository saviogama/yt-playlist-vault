package com.savio.ytplaylistvault.snapshot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.user.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

class ScheduledSnapshotCaptureJobTest {

  private final MonitoredPlaylistRepository monitoredPlaylistRepository =
      Mockito.mock(MonitoredPlaylistRepository.class);
  private final SnapshotCaptureService snapshotCaptureService =
      Mockito.mock(SnapshotCaptureService.class);
  private final OAuth2AuthorizedClientManager authorizedClientManager =
      Mockito.mock(OAuth2AuthorizedClientManager.class);
  private final ScheduledSnapshotCaptureJob scheduledSnapshotCaptureJob =
      new ScheduledSnapshotCaptureJob(
          monitoredPlaylistRepository, snapshotCaptureService, authorizedClientManager);

  @Test
  void capturesAllActivePlaylistsWithOneAuthorizationPerUser() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist firstPlaylist = playlist(user, "first");
    MonitoredPlaylist secondPlaylist = playlist(user, "second");
    OAuth2AuthorizedClient authorizedClient = authorizedClient();

    when(monitoredPlaylistRepository.findByMonitoringStatus(MonitoringStatus.ACTIVE))
        .thenReturn(List.of(firstPlaylist, secondPlaylist));
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
        .thenReturn(authorizedClient);
    SnapshotService.SnapshotCaptureResult captureResult =
        Mockito.mock(SnapshotService.SnapshotCaptureResult.class);
    when(captureResult.created()).thenReturn(false);
    when(snapshotCaptureService.captureSnapshot(any(), any(), any())).thenReturn(captureResult);

    scheduledSnapshotCaptureJob.captureActivePlaylists();

    ArgumentCaptor<OAuth2AuthorizeRequest> authorizeRequestCaptor =
        ArgumentCaptor.forClass(OAuth2AuthorizeRequest.class);
    verify(authorizedClientManager).authorize(authorizeRequestCaptor.capture());
    verify(snapshotCaptureService)
        .captureSnapshot(
            user, firstPlaylist.getId(), authorizedClient.getAccessToken().getTokenValue());
    verify(snapshotCaptureService)
        .captureSnapshot(
            user, secondPlaylist.getId(), authorizedClient.getAccessToken().getTokenValue());

    OAuth2AuthorizeRequest authorizeRequest = authorizeRequestCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(authorizeRequest.getPrincipal().getName())
        .isEqualTo(user.getGoogleSubject());
  }

  @Test
  void skipsUserPlaylistsWhenAuthorizationIsUnavailable() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist playlist = playlist(user, "only");

    when(monitoredPlaylistRepository.findByMonitoringStatus(MonitoringStatus.ACTIVE))
        .thenReturn(List.of(playlist));
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(null);

    scheduledSnapshotCaptureJob.captureActivePlaylists();

    verify(snapshotCaptureService, never()).captureSnapshot(any(), any(), any());
  }

  private MonitoredPlaylist playlist(User user, String suffix) {
    return new MonitoredPlaylist(
        user,
        "playlist-" + suffix,
        "Playlist " + suffix,
        "Playlist used by ScheduledSnapshotCaptureJobTest",
        "https://example.com/playlist.jpg");
  }

  private OAuth2AuthorizedClient authorizedClient() {
    OAuth2AuthorizedClient authorizedClient = Mockito.mock(OAuth2AuthorizedClient.class);
    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "access-token",
            Instant.parse("2026-07-13T00:00:00Z"),
            Instant.parse("2026-07-13T01:00:00Z"));
    when(authorizedClient.getAccessToken()).thenReturn(accessToken);
    return authorizedClient;
  }
}
