package com.project.llt.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.project.llt.constants.ExceptionMessageConstants.DATA_ACCESS_VIOLATION;
import static com.project.llt.constants.ExceptionMessageConstants.HTTP_MEDIA_TYPE_NOT_SUPPORTED;
import static com.project.llt.constants.ExceptionMessageConstants.HTTP_REQUEST_METHOD_NOT_SUPPORTED;
import static com.project.llt.constants.ExceptionMessageConstants.INVALID_REQUEST;
import static com.project.llt.constants.ExceptionMessageConstants.METHOD_ARGUMENT_NOT_VALID;
import static com.project.llt.constants.ExceptionMessageConstants.RESOURCE_NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
        return ResponseEntity
              .status(NOT_FOUND)
              .contentType(APPLICATION_JSON)
              .body(ErrorResponse.builder()
                    .statusCode(NOT_FOUND.value())
                    .message(String.format(RESOURCE_NOT_FOUND, exception.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .build());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException exception) {
        log.error("Invalid request: {}", exception.getMessage());
        return ResponseEntity
              .status(BAD_REQUEST)
              .contentType(APPLICATION_JSON)
              .body(ErrorResponse.builder()
                    .statusCode(BAD_REQUEST.value())
                    .message(String.format(INVALID_REQUEST, exception.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("Http request method not supported: {}", exception.getMessage());
        return ResponseEntity
              .status(BAD_REQUEST)
              .contentType(APPLICATION_JSON)
              .body(ErrorResponse.builder()
                    .statusCode(BAD_REQUEST.value())
                    .message(String.format(HTTP_REQUEST_METHOD_NOT_SUPPORTED, exception.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .build());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        return ResponseEntity
              .status(BAD_REQUEST)
              .contentType(APPLICATION_JSON)
              .body(ErrorResponse.builder()
                    .statusCode(BAD_REQUEST.value())
                    .message(String.format(HTTP_MEDIA_TYPE_NOT_SUPPORTED, exception.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .build());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException exception) {
        log.error(String.format(DATA_ACCESS_VIOLATION, exception.getMessage()));
        return ResponseEntity
              .status(BAD_REQUEST)
              .contentType(APPLICATION_JSON)
              .body(ErrorResponse.builder()
                    .statusCode(BAD_REQUEST.value())
                    .message(String.format(DATA_ACCESS_VIOLATION, exception.getMessage()))
                    .timestamp(LocalDateTime.now())
                    .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.error("Method argument not valid: {}", exception.getMessage());
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity
              .status(BAD_REQUEST)
              .contentType(APPLICATION_JSON)
              .body(ErrorResponse.builder()
                    .statusCode(BAD_REQUEST.value())
                    .message(String.format(METHOD_ARGUMENT_NOT_VALID, exception.getMessage()))
                    .errors(errors)
                    .timestamp(LocalDateTime.now())
                    .build());
    }
}
