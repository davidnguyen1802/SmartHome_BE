package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.request.LoginRequest;
import com.DANN.SmartHome.DTO.request.RefreshTokenRequest;
import com.DANN.SmartHome.DTO.response.LoginResponse;
import com.DANN.SmartHome.domain.entity.RefreshTokenEntity;
import com.DANN.SmartHome.domain.entity.UserEntity;
import com.DANN.SmartHome.domain.repository.RefreshTokenRepository;
import com.DANN.SmartHome.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private AuthServiceImp authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImp(userRepository, refreshTokenRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPasswordHash("hashed-password");
        user.setEnabled(true);

        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "hashed-password")).thenReturn(true);
        when(jwtTokenService.generateAccessToken("admin"))
                .thenReturn(new JwtTokenService.JwtToken("token-value", OffsetDateTime.now().plusDays(1), null));
        when(jwtTokenService.generateRefreshToken("admin"))
                .thenReturn(new JwtTokenService.JwtToken("refresh-token", OffsetDateTime.now().plusDays(7), "refresh-id-1"));

        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertEquals("token-value", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("admin", response.username());
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void loginShouldFailWhenUserDoesNotExist() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(new LoginRequest("ghost", "123456")));
    }

    @Test
    void loginShouldFailWhenPasswordInvalid() {
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPasswordHash("hashed-password");
        user.setEnabled(true);

        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(new LoginRequest("admin", "wrong-password")));
    }

    @Test
    void refreshShouldRotateTokenWhenRefreshTokenIsValid() {
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setEnabled(true);

        RefreshTokenEntity stored = new RefreshTokenEntity();
        stored.setTokenId("refresh-id-1");
        stored.setUsername("admin");
        stored.setTokenHash("ezJXgjPI9ZfSKRCdl7dNTliQNujusuJt2HdKZ0C4Cqc=");
        stored.setExpiresAt(OffsetDateTime.now().plusDays(1));

        when(jwtTokenService.validateRefreshToken("refresh-token-old")).thenReturn(true);
        when(jwtTokenService.extractSubject("refresh-token-old")).thenReturn("admin");
        when(jwtTokenService.extractTokenId("refresh-token-old")).thenReturn("refresh-id-1");
        when(refreshTokenRepository.findByTokenIdAndUsernameIgnoreCase("refresh-id-1", "admin")).thenReturn(Optional.of(stored));
        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

        when(jwtTokenService.generateAccessToken("admin"))
                .thenReturn(new JwtTokenService.JwtToken("access-token-new", OffsetDateTime.now().plusDays(1), null));
        when(jwtTokenService.generateRefreshToken("admin"))
                .thenReturn(new JwtTokenService.JwtToken("refresh-token-new", OffsetDateTime.now().plusDays(7), "refresh-id-2"));
        when(jwtTokenService.extractTokenId("refresh-token-new")).thenReturn("refresh-id-2");

        LoginResponse response = authService.refresh(new RefreshTokenRequest("refresh-token-old"));

        assertEquals("access-token-new", response.accessToken());
        assertEquals("refresh-token-new", response.refreshToken());
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));
    }
}
