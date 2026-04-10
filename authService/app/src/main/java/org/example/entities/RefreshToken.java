package org.example.entities;

import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;



@Entity
@Table(name = "tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity { // Added auditing here too!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Use Long for auto-incrementing IDs

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserInfo userInfo;

    // Helper method to check if the token is dead
    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}