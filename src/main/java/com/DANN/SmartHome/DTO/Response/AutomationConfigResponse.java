package com.DANN.SmartHome.DTO.response;

import java.math.BigDecimal;

public record AutomationConfigResponse(
        BigDecimal fanLowTemp,
        BigDecimal fanHighTemp,
        BigDecimal ledOnThreshold,
        BigDecimal ledOffThreshold,
        Integer pirAlertCooldownSeconds
) {
}
