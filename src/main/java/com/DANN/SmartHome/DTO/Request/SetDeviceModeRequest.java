package com.DANN.SmartHome.DTO.request;

import com.DANN.SmartHome.domain.enums.DeviceMode;
import jakarta.validation.constraints.NotNull;

public record SetDeviceModeRequest(
        @NotNull DeviceMode mode
)
{}
