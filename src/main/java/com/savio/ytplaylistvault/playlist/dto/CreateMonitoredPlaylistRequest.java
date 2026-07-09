package com.savio.ytplaylistvault.playlist.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMonitoredPlaylistRequest(
        @NotBlank String youtubePlaylistId,
        @NotBlank String title,
        String description,
        String thumbnailUrl
) {
    
}
