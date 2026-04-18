package org.example.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.example.entities.UserInfo;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto{

    private String userId;
    private String username;
    private String lastName;
    private String firstName;
    private String email;
    private String phoneNumber;


}
