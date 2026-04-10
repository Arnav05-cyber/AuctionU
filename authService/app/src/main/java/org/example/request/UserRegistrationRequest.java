package org.example.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationRequest {
    private String userName;
    private String email;
    private String password; // This exists ONLY here
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
