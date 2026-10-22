package com.teamnative.moss.domain.auth.service;

import org.springframework.http.HttpStatus;

public class LoginFailureException extends RuntimeException {
    private final HttpStatus status;
    private final Integer failedAttempts;
    private final Integer maxAttempts;

    public LoginFailureException(HttpStatus status, String message, Integer failedAttempts, Integer maxAttempts) {
        super(message);
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.maxAttempts = maxAttempts;
    }

    public HttpStatus getStatus() { return status; }
    public Integer getFailedAttempts() { return failedAttempts; }
    public Integer getMaxAttempts() { return maxAttempts; }
}
