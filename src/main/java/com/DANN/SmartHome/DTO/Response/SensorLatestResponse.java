package com.DANN.SmartHome.DTO.response;

import com.DANN.SmartHome.domain.enums.SensorType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SensorLatestResponse(
        SensorType sensorType,
        BigDecimal value,
        OffsetDateTime receivedAt
) {
}
