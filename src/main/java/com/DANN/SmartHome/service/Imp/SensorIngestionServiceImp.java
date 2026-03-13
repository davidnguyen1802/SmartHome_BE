package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.Internal.SensorEvent;
import com.DANN.SmartHome.domain.entity.SensorLatest;
import com.DANN.SmartHome.domain.entity.SensorReading;
import com.DANN.SmartHome.domain.repository.SensorLatestRepository;
import com.DANN.SmartHome.domain.repository.SensorReadingRepository;
import com.DANN.SmartHome.service.AutomationService;
import com.DANN.SmartHome.service.SensorIngestionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorIngestionServiceImp implements SensorIngestionService {
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorLatestRepository sensorLatestRepository;
    private final AutomationService automationService;

    @Override
    public void handleSensorEvent(SensorEvent event) {
        boolean valid = isValid(event);

        SensorReading reading = new SensorReading();
        reading.setSensorType(event.sensorType());
        reading.setValue(event.value());
        reading.setRawPayload(event.rawPayload());
        reading.setIsValid(valid);
        reading.setValidationMessage(valid ? null : "Value out of range");
        sensorReadingRepository.save(reading);

        if (!valid) {
            return;
        }

        SensorLatest latest = sensorLatestRepository.findById(event.sensorType())
                .orElseGet(() -> {
                    SensorLatest s = new SensorLatest();
                    s.setSensorType(event.sensorType());
                    return s;
                });

        latest.setValue(event.value());
        latest.setRawPayload(event.rawPayload());
        latest.setReceivedAt(event.receivedAt());
        sensorLatestRepository.save(latest);

        automationService.evaluateAfterSensorUpdate(event);
    }

    private boolean isValid(SensorEvent event) {
        if (event == null || event.sensorType() == null || event.value() == null) {
            return false;
        }

        double value = event.value().doubleValue();

        return switch (event.sensorType()) {
            case TEMP -> inRange(value, 15, 40);
            case HUMI -> inRange(value, 0, 100);
            case LIGHT -> inRange(value, 0, 150);
            case PIR -> inRange(value, 0, 1);
        };
    }

    private boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}
