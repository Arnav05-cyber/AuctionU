    package com.example.org.notificationservice.consumers;

    import com.example.org.notificationservice.clients.UserClient;
    import com.example.org.notificationservice.dtos.OtpEventDto;
    import com.example.org.notificationservice.dtos.ProductEvent;
    import com.example.org.notificationservice.dtos.UserResponseDto;
    import jakarta.mail.internet.MimeMessage;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.thymeleaf.context.Context;
    import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;

    import org.springframework.kafka.annotation.DltHandler;
    import org.springframework.kafka.annotation.KafkaListener;

    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.kafka.annotation.RetryableTopic;

    import org.springframework.kafka.support.KafkaHeaders;
    import org.springframework.mail.SimpleMailMessage;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.messaging.handler.annotation.Header;
    import org.springframework.stereotype.Service;
    import org.thymeleaf.TemplateEngine;
    import org.springframework.kafka.annotation.BackOff;
    import org.springframework.beans.factory.annotation.Value;



    @Service
    @Slf4j
    @RequiredArgsConstructor
    public class NotificationConsumer {

        private final UserClient userClient;
        private final JavaMailSender mailSender;
        private final TemplateEngine templateEngine; // Inject this for HTML

        @Value("${spring.mail.username}")
        private String senderEmail;

        @RetryableTopic(
                attempts = "4",
                backOff = @BackOff(delay = 2000, multiplier = 2.0),
                topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
                include = {Exception.class}
        )
        @KafkaListener(topics = "product-events", groupId = "notification-group")
        public void consumeProductEvent(ProductEvent event) {
            // DON'T catch the exception here if you want Retries to work!
            log.info("#### -> Consuming: {} for Product: {}", event.getEventType(), event.getTitle());

            UserResponseDto user = userClient.getUserById(event.getUserId());

            // Let's use the HTML version we discussed
            sendHtmlEmail(event, user);
        }

        @KafkaListener(topics = "otp-events", groupId = "notification-group")
        public void consumeOtpEvent(OtpEventDto event) {
            log.info("#### -> Consumed OTP Event for email: {}", event.getEmail());
            log.info("#### -> LOCAL DEV ONLY - OTP IS: {}", event.getOtp());

            // Create the email content
            String subject = "Your AuctionU Verification Code";
            String body = "Your one-time password is: " + event.getOtp() +
                    "\n\nThis code will expire in 5 minutes.";

            // Use your existing sendEmail method
            sendSimpleEmail(event.getEmail(), subject, body);
        }

        private void sendHtmlEmail(ProductEvent event, UserResponseDto user) {
            try {
                Context context = new Context();
                context.setVariable("userName", user.getName());
                context.setVariable("productTitle", event.getTitle());
                context.setVariable("amount", event.getAmount());

                String htmlContent = templateEngine.process("bid-notification", context);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

                helper.setText(htmlContent, true);
                helper.setTo(user.getEmail());
                helper.setSubject("AuctionU: " + event.getEventType());
                helper.setFrom(senderEmail);

                mailSender.send(mimeMessage);
                log.info("HTML email sent to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error sending mail: {}", e.getMessage());
                throw new RuntimeException(e); // Throwing here triggers the @RetryableTopic
            }
        }

        private void sendSimpleEmail(String to, String subject, String content) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(senderEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(content);

                mailSender.send(message);
                log.info("Plain text email successfully sent to {}", to);
            } catch (Exception e) {
                log.error("Failed to send simple email to {}: {}", to, e.getMessage());
                // You can choose to throw an exception here if you want to retry OTP sends too
            }
        }

        @DltHandler
        public void handleDlt(ProductEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
            log.error("DLT REACHED: Permanent failure for Product ID: {}", event.getProductId());
        }
    }