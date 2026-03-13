package com.DANN.SmartHome.DTO.Internal;

import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;

public record AutomationDecision(
        DeviceType deviceType,
        boolean shouldPublish,
        DeviceState desiredState,
        String payload,
        String reason
) {
    public static AutomationDecision noChange(DeviceType deviceType, String reason) {
        return new AutomationDecision(deviceType, false, null, null, reason);
    }
}
