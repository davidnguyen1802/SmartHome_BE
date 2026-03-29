package com.DANN.SmartHome.service.Imp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    public JwtToken generateAccessToken(String username) {
        return createToken(username, ACCESS_TYPE, null, jwtExpirationMs);
    }

    public JwtToken generateRefreshToken(String username) {
        String tokenId = UUID.randomUUID().toString();
        return createToken(username, REFRESH_TYPE, tokenId, refreshExpirationMs);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, REFRESH_TYPE);
    }

    public String extractSubject(String token) {
        return String.valueOf(readClaims(token).get("sub"));
    }

    public String extractTokenId(String token) {
        Object jti = readClaims(token).get("jti");
        return jti == null ? null : String.valueOf(jti);
    }

    private JwtToken createToken(String username, String tokenType, String tokenId, long expirationMs) {
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = issuedAt.plusNanos(expirationMs * 1_000_000L);

        try {
            String headerJson = objectMapper.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
            Map<String, Object> claims = tokenId == null
                    ? Map.of(
                    "sub", username,
                    "iat", issuedAt.toEpochSecond(),
                    "exp", expiresAt.toEpochSecond(),
                    "type", tokenType
            )
                    : Map.of(
                    "sub", username,
                    "iat", issuedAt.toEpochSecond(),
                    "exp", expiresAt.toEpochSecond(),
                    "type", tokenType,
                    "jti", tokenId
            );
            String payloadJson = objectMapper.writeValueAsString(claims);

            String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String contentToSign = header + "." + payload;
            String signature = sign(contentToSign);

            return new JwtToken(contentToSign + "." + signature, expiresAt, tokenId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate JWT token", ex);
        }
    }

    private boolean validateToken(String token, String expectedType) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String contentToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(contentToSign);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                return false;
            }

            Map<String, Object> claims = readClaims(token);
            Object exp = claims.get("exp");
            Object type = claims.get("type");
            if (exp == null || type == null || !expectedType.equals(String.valueOf(type))) {
                return false;
            }

            long expEpochSeconds = Long.parseLong(String.valueOf(exp));
            return OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond() < expEpochSeconds;
        } catch (Exception ex) {
            return false;
        }
    }

    private Map<String, Object> readClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format");
            }

            byte[] payloadDecoded = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadDecoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token payload", ex);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            byte[] signed = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signed);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT token", ex);
        }
    }

    private String base64UrlEncode(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] first = a.getBytes(StandardCharsets.UTF_8);
        byte[] second = b.getBytes(StandardCharsets.UTF_8);
        if (first.length != second.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < first.length; i++) {
            result |= first[i] ^ second[i];
        }
        return result == 0;
    }

    public record JwtToken(String token, OffsetDateTime expiresAt, String tokenId) {
    }
}
