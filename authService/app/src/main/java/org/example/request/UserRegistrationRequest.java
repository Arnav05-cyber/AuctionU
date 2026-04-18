package org.example.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationRequest {
    @JsonProperty("userName")
    private String username;
    private String email;
    private String password; // This exists ONLY here
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
