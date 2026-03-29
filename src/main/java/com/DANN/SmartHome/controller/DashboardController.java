package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.response.BaseResponse;
import com.DANN.SmartHome.DTO.response.DashboardResponse;
import com.DANN.SmartHome.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        DashboardResponse data = dashboardQueryService.getDashboard();

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Get dashboard successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }
}
