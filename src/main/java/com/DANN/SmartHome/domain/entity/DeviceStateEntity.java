package com.DANN.SmartHome.domain.entity;

import com.DANN.SmartHome.domain.enums.CommandSource;
import com.DANN.SmartHome.domain.enums.DeviceMode;
import com.DANN.SmartHome.domain.enums.DeviceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "device_states")
public class DeviceStateEntity extends AuditEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "device_type", nullable = false, columnDefinition = "device_type_enum")
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "mode", nullable = false, columnDefinition = "device_mode_enum")
    private DeviceMode mode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "state", nullable = false, columnDefinition = "device_state_enum")
    private DeviceStateEntity state;

    @Column(name = "last_command_payload")
    private String lastCommandPayload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "last_command_source", nullable = false, columnDefinition = "command_source_enum")
    private CommandSource lastCommandSource;

    @Column(name = "last_command_reason")
    private String lastCommandReason;

    @Column(name = "last_command_at")
    private OffsetDateTime lastCommandAt;
}