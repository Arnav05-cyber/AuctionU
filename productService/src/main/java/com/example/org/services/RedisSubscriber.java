package com.example.org.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    // This method triggers whenever ANY instance of your service sends a message to Redis
    public void onMessage(String message) {
        try {
            log.info("Redis message received: {}", message);
            // We'll send the message as "productId:amount"
            String[] parts = message.split(":");
            String productId = parts[0];
            String payload = parts[1];

            // Push to the WebSocket topic
            messagingTemplate.convertAndSend("/topic/product/" + productId, payload);
        } catch (Exception e) {
            log.error("Error handling Redis message", e);
        }
    }
}