package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.playlist.dto.CreateMonitoredPlaylistRequest;
import com.savio.ytplaylistvault.playlist.dto.MonitoredPlaylistResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/playlists")
public class MonitoredPlaylistController {
  private final MonitoredPlaylistService monitoredPlaylistService;

  public MonitoredPlaylistController(MonitoredPlaylistService monitoredPlaylistService) {
    this.monitoredPlaylistService = monitoredPlaylistService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MonitoredPlaylistResponse createPlaylist(
      @PathVariable UUID userId, @Valid @RequestBody CreateMonitoredPlaylistRequest request) {
    MonitoredPlaylist playlist = monitoredPlaylistService.createPlaylist(userId, request);
    return MonitoredPlaylistResponse.from(playlist);
  }

  @GetMapping
  public List<MonitoredPlaylistResponse> listPlaylists(@PathVariable UUID userId) {
    return monitoredPlaylistService.listPlaylists(userId).stream()
        .map(MonitoredPlaylistResponse::from)
        .toList();
  }
}
