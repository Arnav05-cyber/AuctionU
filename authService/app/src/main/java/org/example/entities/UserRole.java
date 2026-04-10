package org.example.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
@Getter
@Setter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(nullable = false, unique = true, name = "role_name")
    private String roleName;

}
