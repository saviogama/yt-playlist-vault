package com.savio.ytplaylistvault.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class YoutubeIntegrationException extends RuntimeException {
  private final HttpStatus status;

  private YoutubeIntegrationException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  public static YoutubeIntegrationException fromStatus(HttpStatusCode statusCode) {
    return switch (statusCode.value()) {
      case 401, 403 ->
          new YoutubeIntegrationException(
              HttpStatus.FORBIDDEN,
              "YouTube authorization is missing, invalid, or no longer granted");
      case 404 ->
          new YoutubeIntegrationException(
              HttpStatus.UNPROCESSABLE_CONTENT,
              "The monitored playlist is no longer available on YouTube");
      case 429 ->
          new YoutubeIntegrationException(
              HttpStatus.TOO_MANY_REQUESTS, "YouTube rate limit was reached. Try again later");
      default -> unavailable();
    };
  }

  public static YoutubeIntegrationException unavailable() {
    return new YoutubeIntegrationException(
        HttpStatus.BAD_GATEWAY, "YouTube is temporarily unavailable. Try again later");
  }

  public HttpStatus getStatus() {
    return status;
  }
}
