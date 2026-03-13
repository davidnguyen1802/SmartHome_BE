package com.DANN.SmartHome.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

@Configuration
public class MqttClientConfig {

    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(AdafruitProperties props) {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{props.getMqttBrokerUrl()});
        options.setUserName(props.getUsername());
        options.setPassword(props.getKey().toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        factory.setConnectionOptions(options);
        return factory;
    }
}