package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.domain.entity.DeviceCommandLog;
import com.DANN.SmartHome.domain.entity.DeviceStateEntity;
import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;
import com.DANN.SmartHome.domain.repository.DeviceCommandLogRepository;
import com.DANN.SmartHome.domain.repository.DeviceStateRepository;
import com.DANN.SmartHome.mapper.DeviceStateMapper;
import com.DANN.SmartHome.service.DeviceCommandPublisher;
import com.DANN.SmartHome.service.DeviceControlService;
import com.DANN.SmartHome.DTO.Internal.PublishCommand;
import com.DANN.SmartHome.DTO.Response.DeviceStatusResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceControlServiceImp implements DeviceControlService {

    private final DeviceStateRepository deviceStateRepository;
    private final DeviceCommandLogRepository deviceCommandLogRepository;
    private final DeviceCommandPublisher deviceCommandPublisher;
    private final DeviceStateMapper deviceStateMapper;

    @Override
    public DeviceStatusResponse setMode(DeviceType deviceType, DeviceMode mode) {
        DeviceStateEntity device = getDeviceState(deviceType);

        device.setMode(mode);
        deviceStateRepository.save(device);

        return deviceStateMapper.toResponse(device);
    }

    @Override
    public DeviceStatusResponse executeManualCommand(DeviceType deviceType, DeviceState state, String reason) {
        DeviceStateEntity device = getDeviceState(deviceType);

        // Chống publish trùng nếu đang ở MANUAL và state không đổi
        if (device.getMode() == DeviceMode.MANUAL && device.getState() == state) {
            return deviceStateMapper.toResponse(device);
        }

        String payload = mapPayload(deviceType, state);
        OffsetDateTime now = OffsetDateTime.now();

        // Manual command thì tự chuyển về MANUAL
        device.setMode(DeviceMode.MANUAL);
        device.setState(state);
        device.setLastCommandPayload(payload);
        device.setLastCommandSource(CommandSource.MANUAL_USER);
        device.setLastCommandReason(reason);
        device.setLastCommandAt(now);

        device = deviceStateRepository.save(device);

        deviceCommandPublisher.publish(new PublishCommand(
                deviceType,
                state,
                payload,
                CommandSource.MANUAL_USER,
                reason,
                Map.of()
        ));

        deviceCommandLogRepository.save(buildCommandLog(
                deviceType,
                state,
                payload,
                CommandSource.MANUAL_USER,
                reason
        ));

        return deviceStateMapper.toResponse(device);
    }

    @Override
    public DeviceStatusResponse getDeviceStatus(DeviceType deviceType) {
        DeviceStateEntity deviceStateEntity = getDeviceState(deviceType);
        return deviceStateMapper.toResponse(deviceStateEntity);
    }

    private DeviceStateEntity getDeviceState(DeviceType deviceType) {
        return deviceStateRepository.findById(deviceType)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + deviceType));
    }

    private String mapPayload(DeviceType deviceType, DeviceState state) {
        return switch (deviceType) {
            case LED -> state == DeviceState.ON ? "0" : "1";
            case FAN -> state == DeviceState.ON ? "4" : "5";
        };
    }

    private DeviceCommandLog buildCommandLog(DeviceType deviceType,
                                             DeviceState desiredState,
                                             String payload,
                                             CommandSource source,
                                             String reason) {
        DeviceCommandLog log = new DeviceCommandLog();
        log.setDeviceType(deviceType);
        log.setDesiredState(desiredState);
        log.setCommandPayload(payload);
        log.setCommandSource(source);
        log.setReason(reason);
        log.setSensorSnapshot(Map.of());
        return log;
    }


}

