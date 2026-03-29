package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.response.AutomationConfigResponse;
import com.DANN.SmartHome.DTO.response.DashboardResponse;
import com.DANN.SmartHome.DTO.response.DeviceStatusResponse;
import com.DANN.SmartHome.DTO.response.SensorLatestResponse;
import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;
import com.DANN.SmartHome.domain.enums.SensorType;
import com.DANN.SmartHome.service.DashboardQueryService;
import com.DANN.SmartHome.service.event.DashboardChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardRealtimeServiceImpTest {

    @Mock
    private DashboardQueryService dashboardQueryService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private DashboardRealtimeServiceImp service;

    @BeforeEach
    void setUp() {
        service = new DashboardRealtimeServiceImp(dashboardQueryService, applicationEventPublisher);
        ReflectionTestUtils.setField(service, "realtimeEnabled", true);
        ReflectionTestUtils.setField(service, "emitterTimeoutMs", 0L);
        ReflectionTestUtils.setField(service, "heartbeatIntervalMs", 15000L);
    }

    @Test
    void subscribeShouldReturnEmitterAndPushSnapshot() {
        when(dashboardQueryService.getDashboard()).thenReturn(sampleDashboard());

        SseEmitter emitter = service.subscribe();

        assertNotNull(emitter);
        verify(dashboardQueryService, times(1)).getDashboard();
    }

    @Test
    void publishDashboardChangedShouldPublishEvent() {
        when(dashboardQueryService.getDashboard()).thenReturn(sampleDashboard());

        service.subscribe();

        service.publishDashboardChanged();

        verify(applicationEventPublisher, times(1)).publishEvent(any(DashboardChangedEvent.class));
    }

    @Test
    void subscribeShouldFailWhenRealtimeDisabled() {
        ReflectionTestUtils.setField(service, "realtimeEnabled", false);

        assertThrows(ResponseStatusException.class, () -> service.subscribe());
    }

    private DashboardResponse sampleDashboard() {
        OffsetDateTime now = OffsetDateTime.now();
        return new DashboardResponse(
                new SensorLatestResponse(SensorType.TEMP, BigDecimal.valueOf(30), now),
                new SensorLatestResponse(SensorType.HUMI, BigDecimal.valueOf(70), now),
                new SensorLatestResponse(SensorType.LIGHT, BigDecimal.valueOf(20), now),
                new SensorLatestResponse(SensorType.PIR, BigDecimal.ONE, now),
                new DeviceStatusResponse(DeviceType.LED, DeviceMode.AUTO, DeviceState.ON, "0", CommandSource.AUTOMATION, "LIGHT <= 50", now, now),
                new DeviceStatusResponse(DeviceType.FAN, DeviceMode.AUTO, DeviceState.OFF, "5", CommandSource.AUTOMATION, "TEMP <= lowTemp", now, now),
                new AutomationConfigResponse(BigDecimal.valueOf(26), BigDecimal.valueOf(30), BigDecimal.valueOf(50), BigDecimal.valueOf(70), 30)
        );
    }
}


