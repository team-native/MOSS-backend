package com.teamnative.moss.global.exception;

import com.teamnative.moss.domain.auth.dto.LoginFailureData;
import com.teamnative.moss.domain.auth.service.LoginService.LoginFailureException;
import com.teamnative.moss.global.dto.ApiResponse;
import com.teamnative.moss.global.logging.HttpLoggingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.validation.BindException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LoginFailureException.class)
    public ResponseEntity<ApiResponse<LoginFailureData>> handleLoginFailure(LoginFailureException exception) {
        HttpStatus status = exception.getStatus();
        LoginFailureData data = exception.getFailedAttempts() == null || exception.getMaxAttempts() == null
                ? null
                : new LoginFailureData(exception.getFailedAttempts(), exception.getMaxAttempts());
        ApiResponse<LoginFailureData> response = new ApiResponse<>(false, status.value(), exception.getMessage(), data);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        String message = exception.getReason() != null ? exception.getReason() : HttpStatus.valueOf(status.value()).getReasonPhrase();
        return ResponseEntity.status(status).body(new ApiResponse<>(false, status.value(), message, null));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException exception) {
        HttpStatus status = exception.getStatus();
        return ResponseEntity.status(status).body(ApiResponse.failure(status, exception.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        if (isUniqueConstraintViolation(exception)) {
            return error(HttpStatus.CONFLICT, "이미 사용 중인 값입니다.");
        }
        Object traceId = request.getAttribute(HttpLoggingFilter.TRACE_ID_ATTRIBUTE);
        logger.error("Unexpected data integrity failure. traceId={}", traceId, exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 서버 오류가 발생했습니다.");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
                                                               HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        if (status.is5xxServerError()) {
            Object traceId = request instanceof ServletWebRequest servletWebRequest
                    ? servletWebRequest.getRequest().getAttribute(HttpLoggingFilter.TRACE_ID_ATTRIBUTE)
                    : null;
            logger.error("Unhandled MVC exception. traceId={}", traceId, exception);
        }
        ApiResponse<Void> response = ApiResponse.failure(status, clientMessage(exception, status));
        return super.handleExceptionInternal(exception, response, headers, statusCode, request);
    }

    private String clientMessage(Exception exception, HttpStatus status) {
        if (exception instanceof BindException bindException) {
            return bindException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("요청 값이 올바르지 않습니다.");
        }
        if (exception instanceof HttpRequestMethodNotSupportedException) {
            return "지원하지 않는 HTTP 메서드입니다.";
        }
        if (exception instanceof MissingServletRequestParameterException) {
            return "필수 요청 파라미터가 누락되었습니다.";
        }
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return "요청 파라미터 값이 올바르지 않습니다.";
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return "요청 값이 누락되었거나 올바르지 않습니다.";
        }
        if (exception instanceof HttpMediaTypeNotSupportedException) {
            return "지원하지 않는 요청 형식입니다.";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "요청한 리소스를 찾을 수 없습니다.";
        }
        if (status == HttpStatus.PAYLOAD_TOO_LARGE) {
            return "업로드 용량 제한을 초과했습니다.";
        }
        return switch (status) {
            case BAD_REQUEST -> "요청 값이 올바르지 않습니다.";
            case METHOD_NOT_ALLOWED -> "지원하지 않는 HTTP 메서드입니다.";
            case UNSUPPORTED_MEDIA_TYPE -> "지원하지 않는 요청 형식입니다.";
            default -> status.getReasonPhrase();
        };
    }

    private boolean isUniqueConstraintViolation(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof DuplicateKeyException) {
                return true;
            }
            if (cause instanceof SQLException sqlException
                    && ("23505".equals(sqlException.getSQLState()) || sqlException.getErrorCode() == 1062)) {
                return true;
            }
        }
        return false;
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.failure(status, message));
    }
}
