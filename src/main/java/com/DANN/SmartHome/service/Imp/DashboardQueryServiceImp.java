package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.Response.AutomationConfigResponse;
import com.DANN.SmartHome.DTO.Response.DashboardResponse;
import com.DANN.SmartHome.DTO.Response.DeviceStatusResponse;
import com.DANN.SmartHome.DTO.Response.SensorLatestResponse;
import com.DANN.SmartHome.domain.entity.AutomationConfig;
import com.DANN.SmartHome.domain.entity.DeviceStateEntity;
import com.DANN.SmartHome.domain.entity.SensorLatest;
import com.DANN.SmartHome.domain.enums.DeviceType;
import com.DANN.SmartHome.domain.enums.SensorType;
import com.DANN.SmartHome.domain.repository.AutomationConfigRepository;
import com.DANN.SmartHome.domain.repository.DeviceStateRepository;
import com.DANN.SmartHome.domain.repository.SensorLatestRepository;
import com.DANN.SmartHome.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardQueryServiceImp implements DashboardQueryService {

    private final SensorLatestRepository sensorLatestRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final AutomationConfigRepository automationConfigRepository;

    @Override
    public DashboardResponse getDashboard() {
        SensorLatest temp = getSensor(SensorType.TEMP);
        SensorLatest humi = getSensor(SensorType.HUMI);
        SensorLatest light = getSensor(SensorType.LIGHT);
        SensorLatest pir = getSensor(SensorType.PIR);

        DeviceStateEntity led = getDevice(DeviceType.LED);
        DeviceStateEntity fan = getDevice(DeviceType.FAN);

        AutomationConfig config = automationConfigRepository.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException("Missing automation config"));

        return new DashboardResponse(
                toSensorResponse(temp),
                toSensorResponse(humi),
                toSensorResponse(light),
                toSensorResponse(pir),
                toDeviceResponse(led),
                toDeviceResponse(fan),
                toConfigResponse(config)
        );
    }

    private SensorLatest getSensor(SensorType sensorType) {
        return sensorLatestRepository.findById(sensorType)
                .orElseThrow(() -> new IllegalStateException("Missing sensor latest: " + sensorType));
    }

    private DeviceStateEntity getDevice(DeviceType deviceType) {
        return deviceStateRepository.findById(deviceType)
                .orElseThrow(() -> new IllegalStateException("Missing device state: " + deviceType));
    }

    private SensorLatestResponse toSensorResponse(SensorLatest sensor) {
        return new SensorLatestResponse(
                sensor.getSensorType(),
                sensor.getValue(),
                sensor.getReceivedAt()
        );
    }

    private DeviceStatusResponse toDeviceResponse(DeviceStateEntity entity) {
        return new DeviceStatusResponse(
                entity.getDeviceType(),
                entity.getMode(),
                entity.getState(),
                entity.getLastCommandPayload(),
                entity.getLastCommandSource(),
                entity.getLastCommandReason(),
                entity.getLastCommandAt(),
                entity.getUpdatedAt()
        );
    }

    private AutomationConfigResponse toConfigResponse(AutomationConfig config) {
        return new AutomationConfigResponse(
                config.getFanLowTemp(),
                config.getFanHighTemp(),
                config.getLedOnThreshold(),
                config.getLedOffThreshold(),
                config.getPirAlertCooldownSeconds()
        );
    }
}
