package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    Page<NotificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}