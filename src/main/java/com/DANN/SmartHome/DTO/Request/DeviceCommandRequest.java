package com.DANN.SmartHome.DTO.request;

import com.DANN.SmartHome.domain.enums.DeviceState;
import jakarta.validation.constraints.NotNull;

public record DeviceCommandRequest(
        @NotNull DeviceState state,
        String reason
) {
}
