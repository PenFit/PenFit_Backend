package com.penfit.penfit.global.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.penfit.penfit.global.common.ApiResTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleBusiness(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[{}] {} {} - {}", errorCode.getCode(), request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResTemplate.error(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::describe)
                .collect(Collectors.joining(", "));
        return respond(ErrorCode.INVALID_INPUT, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() != null
                && invalidFormat.getTargetType().isEnum()) {
            return respond(ErrorCode.INVALID_INPUT, describeEnumMismatch(invalidFormat));
        }
        log.warn("요청 본문을 읽을 수 없음: {}", e.getMessage());
        return respond(ErrorCode.MALFORMED_REQUEST, ErrorCode.MALFORMED_REQUEST.getMessage());
    }

    private String describeEnumMismatch(InvalidFormatException e) {
        String field = e.getPath().stream()
                .map(reference -> reference.getFieldName())
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .orElse("값");
        String allowed = Arrays.stream(e.getTargetType().getEnumConstants())
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return "%s: 허용되지 않은 값입니다. 사용 가능한 값 - %s".formatted(field, allowed);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(ErrorCode.INVALID_INPUT, "%s 값이 올바르지 않습니다.".formatted(e.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return respond(ErrorCode.INVALID_INPUT, "%s 값이 필요합니다.".formatted(e.getParameterName()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResTemplate<Void>> handleNoResource(NoResourceFoundException e) {
        return respond(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResTemplate<Void>> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("처리하지 못한 예외 {} {}", request.getMethod(), request.getRequestURI(), e);
        return respond(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    private ResponseEntity<ApiResTemplate<Void>> respond(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResTemplate.error(errorCode, message));
    }

    private String describe(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
