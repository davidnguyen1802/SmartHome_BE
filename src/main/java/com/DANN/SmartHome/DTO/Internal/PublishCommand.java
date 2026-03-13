package com.DANN.SmartHome.DTO.Internal;

import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;

import java.util.Map;

public record PublishCommand(
        DeviceType deviceType,
        DeviceState desiredState,
        String payload,
        CommandSource source,
        String reason,
        Map<String, Object> sensorSnapshot
) {
}
