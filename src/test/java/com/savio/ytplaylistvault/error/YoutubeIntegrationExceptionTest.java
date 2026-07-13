package com.savio.ytplaylistvault.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

class YoutubeIntegrationExceptionTest {

  @ParameterizedTest
  @MethodSource("youtubeStatusMappings")
  void mapsYoutubeStatusToSafeApiResponse(
      HttpStatus youtubeStatus, HttpStatus expectedStatus, String expectedMessage) {
    YoutubeIntegrationException exception = YoutubeIntegrationException.fromStatus(youtubeStatus);

    assertThat(exception.getStatus()).isEqualTo(expectedStatus);
    assertThat(exception.getMessage()).isEqualTo(expectedMessage);
  }

  private static Stream<Arguments> youtubeStatusMappings() {
    return Stream.of(
        Arguments.of(
            HttpStatus.UNAUTHORIZED,
            HttpStatus.FORBIDDEN,
            "YouTube authorization is missing, invalid, or no longer granted"),
        Arguments.of(
            HttpStatus.NOT_FOUND,
            HttpStatus.UNPROCESSABLE_CONTENT,
            "The monitored playlist is no longer available on YouTube"),
        Arguments.of(
            HttpStatus.TOO_MANY_REQUESTS,
            HttpStatus.TOO_MANY_REQUESTS,
            "YouTube rate limit was reached. Try again later"),
        Arguments.of(
            HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.BAD_GATEWAY,
            "YouTube is temporarily unavailable. Try again later"));
  }
}
