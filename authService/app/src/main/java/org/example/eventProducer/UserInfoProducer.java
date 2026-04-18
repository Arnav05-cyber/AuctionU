package org.example.eventProducer;

import org.example.model.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserInfoProducer {
    private final KafkaTemplate<String, UserInfoDto> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    @Autowired
    public UserInfoProducer(KafkaTemplate<String, UserInfoDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(UserInfoDto userInfoDto) {
        try {
            kafkaTemplate.send(topicName, userInfoDto.getUsername(), userInfoDto);
            System.out.println("Sent user event to Kafka: " + userInfoDto.getUsername());
        } catch (Exception e) {
            System.err.println("Failed to send user event to Kafka: " + e.getMessage());
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
