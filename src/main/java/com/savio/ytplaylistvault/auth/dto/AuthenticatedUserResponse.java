package com.savio.ytplaylistvault.auth.dto;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUserResponse(
    UUID userId,
    String googleSubject,
    String email,
    String displayName,
    List<String> grantedScopes,
    boolean youtubeAccessGranted) {}
