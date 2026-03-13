package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.Response.DeviceStatusResponse;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;

public interface DeviceControlService {
    DeviceStatusResponse setMode(DeviceType deviceType, DeviceMode mode);
    DeviceStatusResponse executeManualCommand(DeviceType deviceType, DeviceState state, String reason);
    DeviceStatusResponse getDeviceStatus(DeviceType deviceType);
}
