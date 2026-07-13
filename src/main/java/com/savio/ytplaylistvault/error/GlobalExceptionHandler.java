package com.savio.ytplaylistvault.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiErrorResponse handleResourceNotFound(
      ResourceNotFoundException exception, HttpServletRequest request) {
    return ApiErrorResponse.withoutFields(
        Instant.now(),
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse handleDuplicateResource(
      DuplicateResourceException exception, HttpServletRequest request) {
    return ApiErrorResponse.withoutFields(
        Instant.now(),
        HttpStatus.CONFLICT.value(),
        HttpStatus.CONFLICT.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleValidationError(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<ApiFieldError> fields =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

    return new ApiErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Invalid request",
        request.getRequestURI(),
        fields);
  }

  @ExceptionHandler(YoutubeIntegrationException.class)
  public ResponseEntity<ApiErrorResponse> handleYoutubeIntegrationError(
      YoutubeIntegrationException exception, HttpServletRequest request) {
    HttpStatus status = exception.getStatus();
    ApiErrorResponse response =
        ApiErrorResponse.withoutFields(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            exception.getMessage(),
            request.getRequestURI());

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiErrorResponse handleUnexpectedError(Exception exception, HttpServletRequest request) {
    return ApiErrorResponse.withoutFields(
        Instant.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        "Unexpected internal server error",
        request.getRequestURI());
  }
}
