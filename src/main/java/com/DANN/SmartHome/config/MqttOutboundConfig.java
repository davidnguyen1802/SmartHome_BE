package com.DANN.SmartHome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttOutboundConfig {

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundHandler(
            AdafruitProperties props,
            MqttPahoClientFactory factory
    ) {
        String clientId = props.getUsername() + "-spring-outbound";

        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId, factory);
        handler.setAsync(true);
        handler.setDefaultQos(0);
        handler.setDefaultRetained(false);

        return handler;
    }
}