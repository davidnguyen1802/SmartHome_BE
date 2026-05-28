package com.DANN.SmartHome.controller;

import com.DANN.SmartHome.DTO.response.BaseResponse;
import com.DANN.SmartHome.DTO.response.NotificationResponse;
import com.DANN.SmartHome.domain.entity.NotificationEntity;
import com.DANN.SmartHome.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<NotificationEntity> result = notificationRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));

        List<NotificationResponse> items = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Get notifications successfully");
        response.setData(items);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id) {
        NotificationEntity entity = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));

        entity.setIsRead(true);
        entity.setReadAt(OffsetDateTime.now());
        notificationRepository.save(entity);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Marked as read");
        response.setData(toResponse(entity));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable UUID id) {
        notificationRepository.deleteById(id);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setMessage("Notification deleted");
        return ResponseEntity.ok(response);
    }

    private NotificationResponse toResponse(NotificationEntity e) {
        return new NotificationResponse(
                e.getId(), e.getType(), e.getTitle(), e.getMessage(),
                e.getIsRead(), e.getMetadata(), e.getCreatedAt(), e.getReadAt()
        );
    }
}
