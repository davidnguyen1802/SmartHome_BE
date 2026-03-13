package com.DANN.SmartHome.domain.entity;

import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceState;
import com.DANN.SmartHome.domain.enums.DeviceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "device_command_logs")
public class DeviceCommandLog {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "device_type", nullable = false, columnDefinition = "device_type_enum")
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "desired_state", nullable = false, columnDefinition = "device_state_enum")
    private DeviceState desiredState;

    @Column(name = "command_payload", nullable = false)
    private String commandPayload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "command_source", nullable = false, columnDefinition = "command_source_enum")
    private CommandSource commandSource;

    @Column(name = "reason")
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sensor_snapshot", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> sensorSnapshot;

    @Column(name = "published_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
