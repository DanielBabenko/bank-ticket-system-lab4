package com.example.userservice.config;

import com.example.userservice.event.UserDeletedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public KafkaSender<String, UserDeletedEvent> kafkaSender() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Настройки для надежной доставки (at-least-once)
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ждем подтверждения от всех реплик
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Количество попыток повторной отправки
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Гарантирует отсутствие дублей при retries > 0

        SenderOptions<String, UserDeletedEvent> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }
}