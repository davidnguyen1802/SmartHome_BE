package com.DANN.SmartHome.DTO.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateLedThresholdRequest(
        @NotNull
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0")
        BigDecimal onThreshold,

        @NotNull
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0")
        BigDecimal offThreshold
) {
}
