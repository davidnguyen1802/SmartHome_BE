package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.request.LoginRequest;
import com.DANN.SmartHome.DTO.request.RefreshTokenRequest;
import com.DANN.SmartHome.DTO.response.BaseResponse;
import com.DANN.SmartHome.DTO.response.LoginResponse;
import com.DANN.SmartHome.DTO.response.MeResponse;
import com.DANN.SmartHome.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = authService.login(request);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Login successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse data = authService.refresh(request);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Refresh token successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        MeResponse data = authService.me(String.valueOf(authentication.getPrincipal()));

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Get profile successfully");
        response.setData(data);

        return ResponseEntity.ok(response);
    }
}
