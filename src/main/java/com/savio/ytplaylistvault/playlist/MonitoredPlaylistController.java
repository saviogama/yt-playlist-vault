package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.playlist.dto.CreateMonitoredPlaylistRequest;
import com.savio.ytplaylistvault.playlist.dto.MonitoredPlaylistResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/playlists")
public class MonitoredPlaylistController {
  private final MonitoredPlaylistService monitoredPlaylistService;
  private final UserService userService;

  public MonitoredPlaylistController(
      MonitoredPlaylistService monitoredPlaylistService, UserService userService) {
    this.monitoredPlaylistService = monitoredPlaylistService;
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MonitoredPlaylistResponse createPlaylist(
      @AuthenticationPrincipal OAuth2User oauth2User,
      @Valid @RequestBody CreateMonitoredPlaylistRequest request) {
    User user = syncAuthenticatedUser(oauth2User);

    MonitoredPlaylist playlist = monitoredPlaylistService.createPlaylist(user, request);

    return MonitoredPlaylistResponse.from(playlist);
  }

  @GetMapping
  public List<MonitoredPlaylistResponse> listPlaylists(
      @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    return monitoredPlaylistService.listPlaylists(user).stream()
        .map(MonitoredPlaylistResponse::from)
        .toList();
  }

  private User syncAuthenticatedUser(OAuth2User oauth2User) {
    return userService.syncAuthenticatedUser(
        oauth2User.getAttribute("sub"),
        oauth2User.getAttribute("email"),
        oauth2User.getAttribute("name"));
  }
}
