package com.DANN.SmartHome.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DashboardRealtimeService {
    SseEmitter subscribe();

    void publishDashboardChanged();
}
