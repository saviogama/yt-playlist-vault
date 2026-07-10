package com.savio.ytplaylistvault.user.dto;

import com.savio.ytplaylistvault.user.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String googleSubject,
    String email,
    String displayName,
    Instant createdAt,
    Instant updatedAt) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getGoogleSubject(),
        user.getEmail(),
        user.getDisplayName(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
