package com.DANN.SmartHome.DTO.response;

import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;

import java.time.OffsetDateTime;


public record DeviceStatusResponse(
        DeviceType deviceType,
        DeviceMode mode,
        DeviceState state,
        String lastCommandPayload,
        CommandSource lastCommandSource,
        String lastCommandReason,
        OffsetDateTime lastCommandAt,
        OffsetDateTime updatedAt
) {
}
