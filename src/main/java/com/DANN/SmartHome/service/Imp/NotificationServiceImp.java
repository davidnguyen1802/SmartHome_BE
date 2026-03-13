package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.domain.entity.AutomationConfig;
import com.DANN.SmartHome.domain.entity.NotificationEntity;
import com.DANN.SmartHome.domain.enums.NotificationType;
import com.DANN.SmartHome.domain.repository.AutomationConfigRepository;
import com.DANN.SmartHome.domain.repository.NotificationRepository;
import com.DANN.SmartHome.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AutomationConfigRepository automationConfigRepository;

    @Override
    public void createMotionDetectedNotification() {
        AutomationConfig config = automationConfigRepository.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException("Missing automation config"));

        int cooldownSeconds = config.getPirAlertCooldownSeconds();

        Optional<NotificationEntity> latestOpt =
                notificationRepository.findTopByTypeOrderByCreatedAtDesc(NotificationType.MOTION_DETECTED);

        if (latestOpt.isPresent()) {
            OffsetDateTime lastCreatedAt = latestOpt.get().getCreatedAt();
            if (lastCreatedAt != null &&
                    lastCreatedAt.plusSeconds(cooldownSeconds).isAfter(OffsetDateTime.now())) {
                return;
            }
        }

        NotificationEntity notification = new NotificationEntity();
        notification.setType(NotificationType.MOTION_DETECTED);
        notification.setTitle("Motion detected");
        notification.setMessage("Phát hiện chuyển động !");
        notification.setIsRead(false);
        notification.setMetadata(Map.of("source", "PIR"));

        notificationRepository.save(notification);
    }
}
