package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.request.UpdateFanThresholdRequest;
import com.DANN.SmartHome.DTO.response.AutomationConfigResponse;
import com.DANN.SmartHome.DTO.response.BaseResponse;
import com.DANN.SmartHome.service.AutomationConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/automation")
@RequiredArgsConstructor
public class AutomationController {

    private final AutomationConfigService automationConfigService;

    @GetMapping("/config")
    public ResponseEntity<?> getAutomationConfig() {
        AutomationConfigResponse data = automationConfigService.getConfig();

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Get automation config successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/fan-threshold")
    public ResponseEntity<?> updateFanThreshold(@Valid @RequestBody UpdateFanThresholdRequest request) {
        AutomationConfigResponse data = automationConfigService.updateFanThreshold(
                request.lowTemp(),
                request.highTemp()
        );

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Update fan threshold successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }
}

