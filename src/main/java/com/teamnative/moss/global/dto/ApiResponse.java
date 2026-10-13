package com.teamnative.moss.global.dto;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(boolean success, int status, String message, T data) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(true, status.value(), message, data);
    }

    public static ApiResponse<Void> failure(HttpStatus status, String message) {
        return new ApiResponse<>(false, status.value(), message, null);
    }
}
