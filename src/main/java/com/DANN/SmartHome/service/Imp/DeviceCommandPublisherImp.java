package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.Internal.PublishCommand;
import com.DANN.SmartHome.config.AdafruitProperties;
import com.DANN.SmartHome.service.DeviceCommandPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceCommandPublisherImp implements DeviceCommandPublisher {

    private final MessageChannel mqttOutboundChannel;
    private final AdafruitProperties props;

    @Override
    public void publish(PublishCommand command) {
        String topic = switch (command.deviceType()) {
            case LED -> props.getLedTopic();
            case FAN -> props.getFanTopic();
        };

        mqttOutboundChannel.send(
                MessageBuilder.withPayload(command.payload())
                        .setHeader("mqtt_topic", topic)
                        .build()
        );
    }
}