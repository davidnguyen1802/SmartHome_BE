package com.DANN.SmartHome.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "adafruit.io")
@Data
@Component
public class AdafruitProperties {
    private String username;
    private String key;
    private String mqttBrokerUrl;
    private List<String> sensorTopics;
    private String ledTopic;
    private String fanTopic;
}
