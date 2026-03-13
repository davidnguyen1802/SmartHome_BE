package com.DANN.SmartHome.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttInboundConfig {

    @Bean
    public MqttPahoClientFactory mqttClientFactory(AdafruitProperties props) {
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

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter(
            AdafruitProperties props,
            MqttPahoClientFactory factory
    ) {
        String clientId = props.getUsername() + "-spring-inbound";
        String[] topics = props.getSensorTopics().toArray(new String[0]);

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, factory, topics);

        adapter.setCompletionTimeout(5000);
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel());

        return adapter;
    }
}
