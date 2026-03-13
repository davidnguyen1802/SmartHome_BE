//package com.DANN.SmartHome.mqtt;
//
//import com.DANN.SmartHome.DTO.Internal.SensorEvent;
//import com.DANN.SmartHome.domain.enums.SensorType;
//import com.DANN.SmartHome.service.SensorIngestionService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.messaging.Message;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.OffsetDateTime;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MqttSensorMessageHandler {
//
//    private final SensorIngestionService sensorIngestionService;
//
//    // Chuyển từ @Bean Lambda sang method-level @ServiceActivator
//    @ServiceActivator(inputChannel = "mqttInputChannel")
//    public void handleMqttMessage(Message<?> message) {
//        // BẮT BUỘC PHẢI CÓ TRY-CATCH:
//        // Bất kỳ lỗi nào (như parse số lỗi, lưu DB lỗi...) nếu rò rỉ ra ngoài
//        // sẽ làm Paho MQTT Client tự động ngắt kết nối.
//        try {
//            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
//            String payloadStr = String.valueOf(message.getPayload());
//
//            log.info("🟢 [MQTT INBOUND] Nhận dữ liệu - Topic: {} | Giá trị: {}", topic, payloadStr);
//
//            SensorType sensorType = mapTopicToSensorType(topic);
//
//            SensorEvent event = new SensorEvent(
//                    sensorType,
//                    new BigDecimal(payloadStr),
//                    payloadStr,
//                    OffsetDateTime.now()
//            );
//
//            // Xử lý lưu DB hoặc logic nghiệp vụ
//            sensorIngestionService.handleSensorEvent(event);
//
//        } catch (Exception e) {
//            log.error("🔴 [MQTT ERROR] Xảy ra lỗi khi xử lý dữ liệu MQTT. Lỗi: {}", e.getMessage(), e);
//        }
//    }
//
//    private SensorType mapTopicToSensorType(String topic) {
//        if (topic.endsWith("/bbc-temp")) return SensorType.TEMP;
//        if (topic.endsWith("/bbc-humi")) return SensorType.HUMI;
//        if (topic.endsWith("/bbc-light")) return SensorType.LIGHT;
//        if (topic.endsWith("/bbc-pir")) return SensorType.PIR;
//        throw new IllegalArgumentException("Unsupported MQTT topic: " + topic);
//    }
//}

package com.DANN.SmartHome.mqtt;

import com.DANN.SmartHome.DTO.Internal.SensorEvent;
import com.DANN.SmartHome.domain.enums.SensorType;
import com.DANN.SmartHome.service.SensorIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Configuration
@RequiredArgsConstructor
public class MqttSensorMessageHandler {

    private final SensorIngestionService sensorIngestionService;

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttInboundMessageHandler() {
        return message -> {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = String.valueOf(message.getPayload());

            SensorType sensorType = mapTopicToSensorType(topic);

            SensorEvent event = new SensorEvent(
                    sensorType,
                    new BigDecimal(payload),
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