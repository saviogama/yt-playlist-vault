package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.playlist.dto.CreateMonitoredPlaylistRequest;
import com.savio.ytplaylistvault.playlist.dto.MonitoredPlaylistResponse;
import com.savio.ytplaylistvault.playlist.dto.UpdateMonitoringStatusRequest;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Playlists")
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

  @Operation(
      summary = "Start monitoring a playlist",
      parameters =
          @Parameter(
              name = "X-XSRF-TOKEN",
              in = ParameterIn.HEADER,
              required = true,
              description =
                  "CSRF token returned by GET /api/csrf. Required for state-changing requests."))
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Playlist is now monitored"),
    @ApiResponse(
        responseCode = "409",
        description = "Playlist is already monitored by the authenticated user")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MonitoredPlaylistResponse createPlaylist(
      @AuthenticationPrincipal OAuth2User oauth2User,
      @Valid @RequestBody CreateMonitoredPlaylistRequest request) {
    User user = syncAuthenticatedUser(oauth2User);

    MonitoredPlaylist playlist = monitoredPlaylistService.createPlaylist(user, request);

    return MonitoredPlaylistResponse.from(playlist);
  }

  @Operation(summary = "List monitored playlists")
  @ApiResponse(responseCode = "200", description = "Monitored playlists returned")
  @GetMapping
  public List<MonitoredPlaylistResponse> listPlaylists(
      @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    return monitoredPlaylistService.listPlaylists(user).stream()
        .map(MonitoredPlaylistResponse::from)
        .toList();
  }

  @Operation(
      summary = "Change playlist monitoring status",
      parameters =
          @Parameter(
              name = "X-XSRF-TOKEN",
              in = ParameterIn.HEADER,
              required = true,
              description =
                  "CSRF token returned by GET /api/csrf. Required for state-changing requests."))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Monitoring status updated"),
    @ApiResponse(responseCode = "404", description = "Playlist not found")
  })
  @PatchMapping("/{playlistId}/monitoring-status")
  public MonitoredPlaylistResponse updateMonitoringStatus(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal OAuth2User oauth2User,
      @Valid @RequestBody UpdateMonitoringStatusRequest request) {
    User user = syncAuthenticatedUser(oauth2User);

    MonitoredPlaylist playlist =
        monitoredPlaylistService.updateMonitoringStatus(user, playlistId, request);

    return MonitoredPlaylistResponse.from(playlist);
  }

  private User syncAuthenticatedUser(OAuth2User oauth2User) {
    return userService.syncAuthenticatedUser(
        oauth2User.getAttribute("sub"),
        oauth2User.getAttribute("email"),
        oauth2User.getAttribute("name"));
  }
}
