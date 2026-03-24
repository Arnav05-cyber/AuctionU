package com.arnav.userService.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private String userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
}
