package com.DANN.SmartHome.DTO.Response;

import lombok.*;

@Data
@NoArgsConstructor
public class BaseResponse {
    private int statusCode;
    private String message;
    private Object data;
}
