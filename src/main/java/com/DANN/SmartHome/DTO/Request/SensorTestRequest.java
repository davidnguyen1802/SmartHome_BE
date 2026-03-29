package com.DANN.SmartHome.DTO.request;

import com.DANN.SmartHome.domain.enums.SensorType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SensorTestRequest(
        @NotNull SensorType sensorType,
        @NotNull BigDecimal value
) {
}
