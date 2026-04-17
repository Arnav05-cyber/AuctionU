package com.example.org.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String aggregateId; // Product ID
    private String eventType;   // "BID_PLACED"

    @Column(columnDefinition = "TEXT")
    private String payload;     // The ProductEvent as JSON

    private LocalDateTime createdAt;
    private boolean processed = false;


}
