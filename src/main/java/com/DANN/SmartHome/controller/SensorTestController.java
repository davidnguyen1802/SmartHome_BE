package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.Internal.SensorEvent;
import com.DANN.SmartHome.DTO.Request.SensorTestRequest;
import com.DANN.SmartHome.DTO.Response.BaseResponse;
import com.DANN.SmartHome.service.SensorIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/test/sensors")
@RequiredArgsConstructor
public class SensorTestController {

    private final SensorIngestionService sensorIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestSensor(@Valid @RequestBody SensorTestRequest request) {
        SensorEvent event = new SensorEvent(
                request.sensorType(),
                request.value(),
                request.value().toPlainString(),
                OffsetDateTime.now()
        );

        sensorIngestionService.handleSensorEvent(event);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Ingest sensor event successfully");
        response.setData(null);

        return ResponseEntity.ok(response);
    }
}
