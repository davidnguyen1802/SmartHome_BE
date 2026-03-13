package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.NotificationEntity;
import com.DANN.SmartHome.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    Page<NotificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<NotificationEntity> findTopByTypeOrderByCreatedAtDesc(NotificationType type);
}