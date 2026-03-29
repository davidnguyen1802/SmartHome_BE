package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.request.LoginRequest;
import com.DANN.SmartHome.DTO.request.RefreshTokenRequest;
import com.DANN.SmartHome.DTO.response.LoginResponse;
import com.DANN.SmartHome.DTO.response.MeResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    MeResponse me(String username);
}
