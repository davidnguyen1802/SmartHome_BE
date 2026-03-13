package com.DANN.SmartHome.service;

import com.DANN.SmartHome.DTO.Internal.PublishCommand;

public interface DeviceCommandPublisher {
    void publish(PublishCommand command);
}
