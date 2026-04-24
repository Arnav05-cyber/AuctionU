package org.example.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.model.UserInfoDto;
import org.example.serializer.UserInfoSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ─── Existing: UserInfoDto producer (used by event producers) ─────────────

    @Bean("userInfoProducerFactory")
    public ProducerFactory<String, UserInfoDto> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserInfoSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean("userInfoKafkaTemplate")
    public KafkaTemplate<String, UserInfoDto> kafkaTemplate(@Qualifier("userInfoProducerFactory") ProducerFactory<String, UserInfoDto> pf) {
        return new KafkaTemplate<>(pf);
    }

    // ─── New: Generic Object producer (used by AuthController for OTP events) ──

    @Bean("objectProducerFactory")
    public ProducerFactory<String, Object> objectProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean("objectKafkaTemplate")
    public KafkaTemplate<String, Object> objectKafkaTemplate(@Qualifier("objectProducerFactory") ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
