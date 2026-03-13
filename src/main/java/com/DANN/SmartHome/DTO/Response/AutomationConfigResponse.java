package com.DANN.SmartHome.DTO.Response;

import java.math.BigDecimal;

public record AutomationConfigResponse(
        BigDecimal fanLowTemp,
        BigDecimal fanHighTemp,
        BigDecimal ledOnThreshold,
        BigDecimal ledOffThreshold,
        Integer pirAlertCooldownSeconds
) {
}
