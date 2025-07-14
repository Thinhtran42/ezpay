package com.thinhtran.EzPay.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("Operation completed successfully")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .code("SUCCESS")
                .message(message)
                .build();
    }

    // Error responses
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    // Common error responses
    public static <T> ApiResponse<T> validationError(String message) {
        return error("VALIDATION_ERROR", message);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error("NOT_FOUND", message);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return error("UNAUTHORIZED", message);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return error("FORBIDDEN", message);
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return error("INTERNAL_ERROR", message);
    }
} 