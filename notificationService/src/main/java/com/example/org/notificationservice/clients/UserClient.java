package com.example.org.notificationservice.clients;


import com.example.org.notificationservice.dtos.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/api/v1/users/{id}")
    UserResponseDto getUserById(@PathVariable("id") String id);

}
