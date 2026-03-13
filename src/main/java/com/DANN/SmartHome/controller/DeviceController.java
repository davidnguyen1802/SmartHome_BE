package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.Request.DeviceCommandRequest;
import com.DANN.SmartHome.DTO.Request.SetDeviceModeRequest;
import com.DANN.SmartHome.DTO.Response.BaseResponse;
import com.DANN.SmartHome.DTO.Response.DeviceStatusResponse;
import com.DANN.SmartHome.domain.enums.DeviceType;
import com.DANN.SmartHome.service.DeviceControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceControlService deviceControlService;

    @GetMapping("/{deviceType}")
    public ResponseEntity<?> getDeviceStatus(@PathVariable DeviceType deviceType) {
        DeviceStatusResponse data = deviceControlService.getDeviceStatus(deviceType);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Get device status successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{deviceType}/mode")
    public ResponseEntity<?> setDeviceMode(@PathVariable DeviceType deviceType,
                                           @Valid @RequestBody SetDeviceModeRequest request) {
        DeviceStatusResponse data = deviceControlService.setMode(deviceType, request.mode());

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Update device mode successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deviceType}/command")
    public ResponseEntity<?> executeManualCommand(@PathVariable DeviceType deviceType,
                                                  @Valid @RequestBody DeviceCommandRequest request) {
        DeviceStatusResponse data = deviceControlService.executeManualCommand(
                deviceType,
                request.state(),
                request.reason()
        );

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Execute manual command successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }
}
