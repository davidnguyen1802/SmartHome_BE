package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.request.LoginRequest;
import com.DANN.SmartHome.DTO.request.RefreshTokenRequest;
import com.DANN.SmartHome.DTO.response.BaseResponse;
import com.DANN.SmartHome.DTO.response.LoginResponse;
import com.DANN.SmartHome.DTO.response.MeResponse;
import com.DANN.SmartHome.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void loginShouldReturnBaseResponse() {
        when(authService.login(new LoginRequest("admin", "admin123")))
                .thenReturn(new LoginResponse(
                        "sample-token",
                        "sample-refresh-token",
                        "Bearer",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().plusDays(7),
                        "admin"
                ));

        ResponseEntity<?> responseEntity = authController.login(new LoginRequest("admin", "admin123"));

        assertEquals(200, responseEntity.getStatusCode().value());
        BaseResponse body = (BaseResponse) responseEntity.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatusCode());
        assertEquals("Login successfully", body.getMessage());

        LoginResponse data = (LoginResponse) body.getData();
        assertNotNull(data);
        assertEquals("sample-token", data.accessToken());
        assertEquals("sample-refresh-token", data.refreshToken());
        assertEquals("Bearer", data.tokenType());
        assertEquals("admin", data.username());
    }

    @Test
    void refreshShouldReturnBaseResponse() {
        when(authService.refresh(new RefreshTokenRequest("old-refresh")))
                .thenReturn(new LoginResponse(
                        "new-access",
                        "new-refresh",
                        "Bearer",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().plusDays(7),
                        "admin"
                ));

        ResponseEntity<?> responseEntity = authController.refresh(new RefreshTokenRequest("old-refresh"));

        assertEquals(200, responseEntity.getStatusCode().value());
        BaseResponse body = (BaseResponse) responseEntity.getBody();
        assertNotNull(body);
        assertEquals("Refresh token successfully", body.getMessage());
    }

    @Test
    void meShouldReturnAuthenticatedProfile() {
        when(authService.me("admin")).thenReturn(new MeResponse(1L, "admin", true));

        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", null);
        ResponseEntity<?> responseEntity = authController.me(authentication);

        assertEquals(200, responseEntity.getStatusCode().value());
        BaseResponse body = (BaseResponse) responseEntity.getBody();
        assertNotNull(body);
        assertEquals("Get profile successfully", body.getMessage());

        MeResponse data = (MeResponse) body.getData();
        assertEquals("admin", data.username());
    }
}



