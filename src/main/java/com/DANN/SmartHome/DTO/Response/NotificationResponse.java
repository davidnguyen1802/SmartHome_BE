package com.DANN.SmartHome.DTO.Response;

import com.DANN.SmartHome.domain.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        Boolean isRead,
        Map<String, Object> metadata,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
}
