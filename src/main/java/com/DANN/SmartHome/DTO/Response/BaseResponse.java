package com.DANN.SmartHome.DTO.response;

import lombok.*;

@Data
@NoArgsConstructor
public class BaseResponse {
    private int statusCode;
    private String message;
    private Object data;
}
