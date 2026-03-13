package com.DANN.SmartHome.DTO.Response;

public record BaseResponse<T>(
        int statusCode,
        String message,
        T data
) {
    public static <T> BaseResponse<T> of(int statusCode, String message, T data) {
        return new BaseResponse<>(statusCode, message, data);
    }
}
