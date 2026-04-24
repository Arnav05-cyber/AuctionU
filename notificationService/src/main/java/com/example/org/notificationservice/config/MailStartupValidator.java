package com.example.org.notificationservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailStartupValidator {

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @PostConstruct
    public void validateMailConfig() {
        if (isBlank(mailUsername) || isBlank(mailPassword)) {
            log.warn("Mail credentials are missing. Set MAIL_USERNAME/MAIL_PASSWORD or SPRING_MAIL_USERNAME/SPRING_MAIL_PASSWORD. OTP emails will fail until configured.");
            return;
        }

        log.info("Mail credentials detected for sender '{}'.", mailUsername);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
