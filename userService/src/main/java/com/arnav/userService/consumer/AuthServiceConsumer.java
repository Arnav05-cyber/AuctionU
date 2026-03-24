package com.arnav.userService.consumer;

import com.arnav.userService.dtos.UserDto;
import com.arnav.userService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceConsumer {

    private final UserService userService;

    @Autowired
    public AuthServiceConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeMessage(
            @Payload(required = false) UserDto eventData
    ) {
        if (eventData == null) {
            System.out.println("⚠️ Received tombstone (null payload). Skipping.");
            return;
        }

        System.out.println("EVENT CONSUMED IN USER SERVICE: " + eventData);
        userService.createOrUpdateUser(eventData);
    }
}
