package com.DANN.SmartHome.domain.entity;

import com.DANN.SmartHome.domain.enums.SensorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "sensor_latest")
public class SensorLatest extends AuditEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sensor_type", nullable = false, columnDefinition = "sensor_type_enum")
    private SensorType sensorType;

    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;
}
