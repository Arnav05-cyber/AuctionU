package com.example.org.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ProductEvent {

    private String eventType;
    private Long productId;
    private String title;
    private BigDecimal amount;
    private String userId;
    private String sellerId;
    private LocalDateTime timeStamp;

}
