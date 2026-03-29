package com.DANN.SmartHome.DTO.response;

import java.time.OffsetDateTime;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        OffsetDateTime expiresAt,
        OffsetDateTime refreshExpiresAt,
        String username
) {
}
