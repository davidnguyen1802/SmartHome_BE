package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.Internal.SensorEvent;

public interface SensorIngestionService {
    void handleSensorEvent(SensorEvent event);
}
