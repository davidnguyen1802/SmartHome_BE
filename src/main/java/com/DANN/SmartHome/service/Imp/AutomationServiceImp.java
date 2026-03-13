package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.Internal.PublishCommand;
import com.DANN.SmartHome.DTO.Internal.SensorEvent;
import com.DANN.SmartHome.domain.entity.AutomationConfig;
import com.DANN.SmartHome.domain.entity.DeviceCommandLog;
import com.DANN.SmartHome.domain.entity.DeviceStateEntity;
import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;
import com.DANN.SmartHome.domain.repository.AutomationConfigRepository;
import com.DANN.SmartHome.domain.repository.DeviceCommandLogRepository;
import com.DANN.SmartHome.domain.repository.DeviceStateRepository;
import com.DANN.SmartHome.service.AutomationService;
import com.DANN.SmartHome.service.DeviceCommandPublisher;
import com.DANN.SmartHome.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomationServiceImp implements AutomationService {

    private final DeviceStateRepository deviceStateRepository;
    private final AutomationConfigRepository automationConfigRepository;
    private final DeviceCommandPublisher deviceCommandPublisher;
    private final DeviceCommandLogRepository deviceCommandLogRepository;
    private final NotificationService notificationService;

    @Override
    public void evaluateAfterSensorUpdate(SensorEvent event) {
        switch (event.sensorType()) {
            case LIGHT -> evaluateLed(event.value());
            case TEMP -> evaluateFan(event.value());
            case PIR -> evaluatePir(event.value());
            case HUMI -> {
                // Chưa có logic tự động cho HUMI
            }
        }
    }

    private void evaluateLed(BigDecimal lightValue) {
        DeviceStateEntity led = getDevice(DeviceType.LED);
        if (led.getMode() != DeviceMode.AUTO) {
            return;
        }

        if (lightValue.compareTo(new BigDecimal("50")) <= 0) {
            publishIfChanged(led, DeviceState.ON, "0", "LIGHT <= 50");
        } else if (lightValue.compareTo(new BigDecimal("70")) >= 0) {
            publishIfChanged(led, DeviceState.OFF, "1", "LIGHT >= 70");
        }
    }

    private void evaluateFan(BigDecimal tempValue) {
        DeviceStateEntity fan = getDevice(DeviceType.FAN);
        if (fan.getMode() != DeviceMode.AUTO) {
            return;
        }

        AutomationConfig config = getAutomationConfig();

        if (tempValue.compareTo(config.getFanLowTemp()) <= 0) {
            publishIfChanged(fan, DeviceState.OFF, "5", "TEMP <= lowTemp");
        } else if (tempValue.compareTo(config.getFanHighTemp()) >= 0) {
            publishIfChanged(fan, DeviceState.ON, "4", "TEMP >= highTemp");
        }
    }

    private void evaluatePir(BigDecimal pirValue) {
        if (pirValue.compareTo(BigDecimal.ONE) == 0) {
            notificationService.createMotionDetectedNotification();
        }
    }

    private void publishIfChanged(DeviceStateEntity device,
                                  DeviceState desiredState,
                                  String payload,
                                  String reason) {
        if (device.getState() == desiredState) {
            return;
        }

        device.setState(desiredState);
        device.setLastCommandPayload(payload);
        device.setLastCommandSource(CommandSource.AUTOMATION);
        device.setLastCommandReason(reason);
        device.setLastCommandAt(OffsetDateTime.now());
        deviceStateRepository.save(device);

        deviceCommandPublisher.publish(new PublishCommand(
                device.getDeviceType(),
                desiredState,
                payload,
                CommandSource.AUTOMATION,
                reason,
                Map.of()
        ));

        DeviceCommandLog log = new DeviceCommandLog();
        log.setDeviceType(device.getDeviceType());
        log.setDesiredState(desiredState);
        log.setCommandPayload(payload);
        log.setCommandSource(CommandSource.AUTOMATION);
        log.setReason(reason);
        log.setSensorSnapshot(Map.of());
        deviceCommandLogRepository.save(log);
    }

    private DeviceStateEntity getDevice(DeviceType type) {
        return deviceStateRepository.findById(type)
                .orElseThrow(() -> new IllegalStateException("Missing device state for: " + type));
    }

    private AutomationConfig getAutomationConfig() {
        return automationConfigRepository.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException("Missing automation config"));
    }
}
