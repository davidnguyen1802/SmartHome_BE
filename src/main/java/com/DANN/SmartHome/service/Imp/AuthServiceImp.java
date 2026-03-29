package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.request.LoginRequest;
import com.DANN.SmartHome.DTO.request.RefreshTokenRequest;
import com.DANN.SmartHome.DTO.response.LoginResponse;
import com.DANN.SmartHome.DTO.response.MeResponse;
import com.DANN.SmartHome.domain.entity.RefreshTokenEntity;
import com.DANN.SmartHome.domain.entity.UserEntity;
import com.DANN.SmartHome.domain.repository.RefreshTokenRepository;
import com.DANN.SmartHome.domain.repository.UserRepository;
import com.DANN.SmartHome.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new IllegalArgumentException("User is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return issueTokenPair(user.getUsername());
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (!jwtTokenService.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtTokenService.extractSubject(refreshToken);
        String tokenId = jwtTokenService.extractTokenId(refreshToken);
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshTokenEntity persistedToken = refreshTokenRepository.findByTokenIdAndUsernameIgnoreCase(tokenId, username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (persistedToken.getRevokedAt() != null
                || persistedToken.getExpiresAt().isBefore(OffsetDateTime.now())
                || !constantTimeEquals(hashRefreshToken(refreshToken), persistedToken.getTokenHash())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LoginResponse newTokens = issueTokenPair(user.getUsername());
        String newTokenId = jwtTokenService.extractTokenId(newTokens.refreshToken());

        persistedToken.setRevokedAt(OffsetDateTime.now());
        persistedToken.setReplacedByTokenId(newTokenId);
        refreshTokenRepository.save(persistedToken);

        return newTokens;
    }

    @Override
    public MeResponse me(String username) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new MeResponse(user.getId(), user.getUsername(), user.getEnabled());
    }

    private LoginResponse issueTokenPair(String username) {
        JwtTokenService.JwtToken accessToken = jwtTokenService.generateAccessToken(username);
        JwtTokenService.JwtToken refreshToken = jwtTokenService.generateRefreshToken(username);

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setTokenId(refreshToken.tokenId());
        refreshTokenEntity.setUsername(username);
        refreshTokenEntity.setTokenHash(hashRefreshToken(refreshToken.token()));
        refreshTokenEntity.setExpiresAt(refreshToken.expiresAt());
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(
                accessToken.token(),
                refreshToken.token(),
                "Bearer",
                accessToken.expiresAt(),
                refreshToken.expiresAt(),
                username
        );
    }

    private String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
