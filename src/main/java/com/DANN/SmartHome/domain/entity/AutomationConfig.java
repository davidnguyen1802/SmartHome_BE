package com.DANN.SmartHome.domain.entity;

import com.DANN.SmartHome.domain.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "automation_configs")
public class AutomationConfig extends AuditEntity {

    @Id
    @Column(name = "id")
    private Short id;

    @Column(name = "fan_low_temp", nullable = false, precision = 5, scale = 2)
    private BigDecimal fanLowTemp;

    @Column(name = "fan_high_temp", nullable = false, precision = 5, scale = 2)
    private BigDecimal fanHighTemp;

    @Column(name = "led_on_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal ledOnThreshold;

    @Column(name = "led_off_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal ledOffThreshold;

    @Column(name = "pir_alert_cooldown_seconds", nullable = false)
    private Integer pirAlertCooldownSeconds;
}