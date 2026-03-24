package com.arnav.userService.deserializer;

import com.arnav.userService.dtos.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class  UserInfoDeserializer implements Deserializer<UserDto> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No configuration needed
    }

    @Override
    public UserDto deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null; // IMPORTANT
        }

        try {
            return objectMapper.readValue(data, UserDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing UserInfoDto", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }

}
