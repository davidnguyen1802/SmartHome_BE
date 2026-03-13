package com.DANN.SmartHome.domain.entity;

import com.DANN.SmartHome.domain.enums.SensorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sensor_type", nullable = false, columnDefinition = "sensor_type_enum")
    private SensorType sensorType;

    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    @Column(name = "validation_message")
    private String validationMessage;

    @Column(name = "received_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
