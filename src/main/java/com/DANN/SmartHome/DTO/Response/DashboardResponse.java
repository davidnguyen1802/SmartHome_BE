package com.DANN.SmartHome.DTO.response;

public record DashboardResponse(
        SensorLatestResponse temp,
        SensorLatestResponse humi,
        SensorLatestResponse light,
        SensorLatestResponse pir,
        DeviceStatusResponse led,
        DeviceStatusResponse fan,
        AutomationConfigResponse automationConfig
) {
}
