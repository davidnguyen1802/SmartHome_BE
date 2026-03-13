package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.Response.DeviceStatusResponse;
import com.DANN.SmartHome.domain.entity.DeviceStateEntity;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceType;
import com.DANN.SmartHome.domain.repository.DeviceCommandLogRepository;
import com.DANN.SmartHome.domain.repository.DeviceStateRepository;
import com.DANN.SmartHome.service.DeviceCommandPublisher;
import com.DANN.SmartHome.service.DeviceControlService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceControlServiceImp implements DeviceControlService {
    private final DeviceStateRepository deviceStateRepository;
    private final DeviceCommandLogRepository deviceCommandLogRepository;
    private final DeviceCommandPublisher deviceCommandPublisher;

    @Override
    public DeviceStatusResponse setMode(DeviceType deviceType, DeviceMode mode) {
        DeviceStateEntity device = getDeviceState(deviceType);
        device.setMode(mode);
        deviceStateRepository.save(device);
        return toResponse(device);
    }

    private DeviceStateEntity getDeviceState(DeviceType deviceType) {
        return deviceStateRepository.findById(deviceType)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + deviceType));
    }
}

