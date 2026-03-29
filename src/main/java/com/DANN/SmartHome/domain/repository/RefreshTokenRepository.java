package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenIdAndUsernameIgnoreCase(String tokenId, String username);
}
