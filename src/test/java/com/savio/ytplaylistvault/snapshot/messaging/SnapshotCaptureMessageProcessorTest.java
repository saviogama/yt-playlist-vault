package com.savio.ytplaylistvault.snapshot.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.snapshot.SnapshotCaptureService;
import com.savio.ytplaylistvault.snapshot.SnapshotService;
import com.savio.ytplaylistvault.user.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

class SnapshotCaptureMessageProcessorTest {

  private final MonitoredPlaylistRepository monitoredPlaylistRepository =
      Mockito.mock(MonitoredPlaylistRepository.class);
  private final SnapshotCaptureService snapshotCaptureService =
      Mockito.mock(SnapshotCaptureService.class);
  private final OAuth2AuthorizedClientManager authorizedClientManager =
      Mockito.mock(OAuth2AuthorizedClientManager.class);
  private final SnapshotCaptureMessageProcessor processor =
      new SnapshotCaptureMessageProcessor(
          monitoredPlaylistRepository, snapshotCaptureService, authorizedClientManager);

  @Test
  void capturesActivePlaylistWhenAuthorizationIsAvailable() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist playlist = playlist(user);
    OAuth2AuthorizedClient authorizedClient = authorizedClient();
    SnapshotService.SnapshotCaptureResult captureResult =
        Mockito.mock(SnapshotService.SnapshotCaptureResult.class);

    when(monitoredPlaylistRepository.findById(playlist.getId())).thenReturn(Optional.of(playlist));
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
        .thenReturn(authorizedClient);
    when(captureResult.created()).thenReturn(false);
    when(snapshotCaptureService.captureSnapshot(any(), any(), any())).thenReturn(captureResult);

    processor.process(new SnapshotCaptureRequested(playlist.getId()));

    verify(snapshotCaptureService)
        .captureSnapshot(user, playlist.getId(), authorizedClient.getAccessToken().getTokenValue());
  }

  @Test
  void skipsPausedPlaylist() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist playlist = playlist(user);
    playlist.changeMonitoringStatus(MonitoringStatus.PAUSED);

    when(monitoredPlaylistRepository.findById(playlist.getId())).thenReturn(Optional.of(playlist));

    processor.process(new SnapshotCaptureRequested(playlist.getId()));

    verifyNoInteractions(authorizedClientManager, snapshotCaptureService);
  }

  @Test
  void skipsWhenAuthorizationIsUnavailable() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist playlist = playlist(user);

    when(monitoredPlaylistRepository.findById(playlist.getId())).thenReturn(Optional.of(playlist));
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(null);

    processor.process(new SnapshotCaptureRequested(playlist.getId()));

    verify(snapshotCaptureService, never()).captureSnapshot(any(), any(), any());
  }

  @Test
  void skipsWhenPlaylistNoLongerExists() {
    when(monitoredPlaylistRepository.findById(any())).thenReturn(Optional.empty());

    processor.process(new SnapshotCaptureRequested(java.util.UUID.randomUUID()));

    verifyNoInteractions(authorizedClientManager, snapshotCaptureService);
  }

  private MonitoredPlaylist playlist(User user) {
    return new MonitoredPlaylist(
        user,
        "provider-playlist-id",
        "Test Playlist",
        "Playlist used by SnapshotCaptureMessageProcessorTest",
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
