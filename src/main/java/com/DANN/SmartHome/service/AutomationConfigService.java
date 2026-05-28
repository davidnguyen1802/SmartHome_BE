package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.response.AutomationConfigResponse;

public interface AutomationConfigService {
    AutomationConfigResponse getConfig();
    AutomationConfigResponse updateFanThreshold(java.math.BigDecimal lowTemp, java.math.BigDecimal highTemp);
    AutomationConfigResponse updateLedThreshold(java.math.BigDecimal onThreshold, java.math.BigDecimal offThreshold);
}
