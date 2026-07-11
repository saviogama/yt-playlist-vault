package com.savio.ytplaylistvault.youtube.dto;

public record YoutubePlaylistResponse(
    String providerPlaylistId, String title, String description, String thumbnailUrl) {}
