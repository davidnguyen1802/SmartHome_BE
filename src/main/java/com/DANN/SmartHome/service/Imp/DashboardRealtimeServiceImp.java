package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.response.DashboardResponse;
import com.DANN.SmartHome.service.DashboardQueryService;
import com.DANN.SmartHome.service.DashboardRealtimeService;
import com.DANN.SmartHome.service.event.DashboardChangedEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DashboardRealtimeServiceImp implements DashboardRealtimeService {

    private final DashboardQueryService dashboardQueryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Value("${dashboard.realtime.enabled:true}")
    private boolean realtimeEnabled;

    @Value("${dashboard.realtime.emitter-timeout-ms:0}")
    private long emitterTimeoutMs;

    @Value("${dashboard.realtime.heartbeat-interval-ms:15000}")
    private long heartbeatIntervalMs;

    private ScheduledExecutorService heartbeatExecutor;

    @PostConstruct
    void startHeartbeat() {
        if (!realtimeEnabled) {
            return;
        }

        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(
                this::sendHeartbeat,
                heartbeatIntervalMs,
                heartbeatIntervalMs,
                TimeUnit.MILLISECONDS
        );
    }

    @PreDestroy
    void stopHeartbeat() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
    }

    @Override
    public SseEmitter subscribe() {
        if (!realtimeEnabled) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dashboard realtime is disabled");
        }

        long timeout = emitterTimeoutMs <= 0 ? 0L : emitterTimeoutMs;
        SseEmitter emitter = new SseEmitter(timeout);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(error -> {
            emitter.complete();
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        sendSnapshotToEmitter(emitter);
        return emitter;
    }

    @Override
    public void publishDashboardChanged() {
        if (!realtimeEnabled || emitters.isEmpty()) {
            return;
        }

        applicationEventPublisher.publishEvent(new DashboardChangedEvent());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDashboardChanged(DashboardChangedEvent event) {
        if (emitters.isEmpty()) {
            return;
        }

        DashboardResponse snapshot = dashboardQueryService.getDashboard();
        for (SseEmitter emitter : emitters) {
            sendEvent(emitter, "dashboard.snapshot", snapshot);
        }
    }

    private void sendSnapshotToEmitter(SseEmitter emitter) {
        DashboardResponse snapshot = dashboardQueryService.getDashboard();
        sendEvent(emitter, "dashboard.snapshot", snapshot);
    }

    private void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            sendEvent(emitter, "heartbeat", "ok");
        }
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException | IllegalStateException ex) {
            emitter.complete();
            emitters.remove(emitter);
        }
    }
}

