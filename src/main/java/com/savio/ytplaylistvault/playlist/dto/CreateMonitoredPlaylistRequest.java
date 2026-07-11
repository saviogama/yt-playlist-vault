package com.savio.ytplaylistvault.playlist.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMonitoredPlaylistRequest(
    @NotBlank String providerPlaylistId,
    @NotBlank String title,
    String description,
    String thumbnailUrl) {}
