package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.Response.AutomationConfigResponse;

public interface AutomationConfigService {
    AutomationConfigResponse getConfig();
    AutomationConfigResponse updateFanThreshold(java.math.BigDecimal lowTemp, java.math.BigDecimal highTemp);
}
