package com.DANN.SmartHome.mqtt;

import com.DANN.SmartHome.DTO.Internal.SensorEvent;
import com.DANN.SmartHome.domain.enums.SensorType;
import com.DANN.SmartHome.service.SensorIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Configuration
@RequiredArgsConstructor
public class MqttSensorMessageHandler {

    private final SensorIngestionService sensorIngestionService;

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = String.valueOf(message.getPayload());

            SensorType sensorType = mapTopicToSensorType(topic);
            BigDecimal value = new BigDecimal(payload);

            SensorEvent event = new SensorEvent(
                    sensorType,
                    value,
                    payload,
                    OffsetDateTime.now()
            );

            sensorIngestionService.handleSensorEvent(event);
        };
    }

    private SensorType mapTopicToSensorType(String topic) {
        if (topic.endsWith("/bbc-temp")) return SensorType.TEMP;
        if (topic.endsWith("/bbc-humi")) return SensorType.HUMI;
        if (topic.endsWith("/bbc-light")) return SensorType.LIGHT;
        if (topic.endsWith("/bbc-pir")) return SensorType.PIR;
        throw new IllegalArgumentException("Unsupported MQTT topic: " + topic);
    }
}
