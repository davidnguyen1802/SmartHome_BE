package com.DANN.SmartHome.DTO.Internal;

import com.DANN.SmartHome.domain.enums.SensorType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SensorEvent(
        SensorType sensorType,
        BigDecimal value,
        String rawPayload,
        OffsetDateTime receivedAt
) {
}
