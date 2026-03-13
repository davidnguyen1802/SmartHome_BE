package com.DANN.SmartHome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttInboundConfig {

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

        //FIX: RACE CONDITION Đặt phase cao để Adapter khởi động sau cùng, đảm bảo Subscriber đã sẵn sàng hứng dữ liệu
        // adapter.setPhase(100);

        return adapter;
    }
}