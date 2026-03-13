package com.DANN.SmartHome.Util;

import com.DANN.SmartHome.DTO.Response.BaseResponse;
import org.springframework.http.ResponseEntity;

public class ResponseCentral {

    private ResponseCentral() {}

    public static ResponseEntity<BaseResponse> success(int statusCode, String message, Object data) {
        BaseResponse response = new BaseResponse();
        response.setStatusCode(statusCode);
        response.setMessage(message);
        response.setData(data);
        return ResponseEntity.status(statusCode).body(response);
    }

    public static ResponseEntity<BaseResponse> ok(String message, Object data) {
        return success(200, message, data);
    }
}
