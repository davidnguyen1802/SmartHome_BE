package com.DANN.SmartHome.mapper;

import com.DANN.SmartHome.DTO.response.DeviceStatusResponse;
import com.DANN.SmartHome.domain.entity.DeviceStateEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface DeviceStateMapper {
    DeviceStatusResponse toResponse(DeviceStateEntity entity);
}
