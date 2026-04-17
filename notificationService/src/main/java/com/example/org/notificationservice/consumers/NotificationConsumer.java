package com.example.org.notificationservice.consumers;

import com.example.org.notificationservice.clients.UserClient;
import com.example.org.notificationservice.dtos.ProductEvent;
import com.example.org.notificationservice.dtos.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender; // 1. Import this
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final UserClient userClient;
    private final JavaMailSender mailSender; // 2. Inject it

    @KafkaListener(topics = "product-events", groupId = "notification-group")
    public void consumeProductEvent(ProductEvent event) {
        try {
            UserResponseDto user = userClient.getUserById(event.getUserId());
            handleNotification(event, user);
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage());
        }
    }

    private void handleNotification(ProductEvent event, UserResponseDto user) {
        String subject = switch (event.getEventType()) {
            case "BID_PLACED" -> "Bid Confirmed: " + event.getTitle();
            case "PURCHASE" -> "Order Confirmation: " + event.getTitle();
            case "AUCTION_CLOSED" -> "Winner! You won: " + event.getTitle();
            default -> "Update from AuctionU";
        };

        String body = "Hi " + user.getName() + ",\n\n" +
                "Update regarding: " + event.getTitle() + "\n" +
                "Amount: $" + event.getAmount() + "\n\n" +
                "Thank you for using AuctionU!";

        sendEmail(user.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        // 3. Create and send the message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com"); // Must match username in YML
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("Email successfully sent to {}", to);
    }
}