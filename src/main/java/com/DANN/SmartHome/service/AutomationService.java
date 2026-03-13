package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.Internal.SensorEvent;

public interface AutomationService {
    void evaluateAfterSensorUpdate(SensorEvent event);
}
