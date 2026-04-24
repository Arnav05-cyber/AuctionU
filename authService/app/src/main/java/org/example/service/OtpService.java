package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.request.UserRegistrationRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String PREFIX = "OTP:";

    public String generateOtp(String email) {
        // Generate a secure 6-digit code
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));

        // Store in Redis for 5 minutes
        redisTemplate.opsForValue().set(PREFIX + email, otp, 5, TimeUnit.MINUTES);

        return otp;
    }

    public boolean validateOtp(String email, String userOtp) {
        String storedOtp = redisTemplate.opsForValue().get(PREFIX + email);
        return storedOtp != null && storedOtp.equals(userOtp);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    private static final String USER_PREFIX = "PENDING_USER:";

    public void saveTemporaryUser(String email, UserRegistrationRequest data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(USER_PREFIX + email, json, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize user data");
        }
    }

    public UserRegistrationRequest getTemporaryUser(String email) {
        String json = redisTemplate.opsForValue().get(USER_PREFIX + email);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserRegistrationRequest.class);
        } catch (Exception e) {
            return null;
        }
    }
}