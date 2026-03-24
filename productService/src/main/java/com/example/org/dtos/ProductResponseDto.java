package com.example.org.dtos;

import com.example.org.enums.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    private String description;
    private String category;

    @PositiveOrZero
    private BigDecimal startingPrice;

    @PositiveOrZero
    private BigDecimal currentHighestBid;

    @NotNull
    private LocalDateTime auctionEndTime;

    private LocalDateTime createdAt;

    @NotNull
    private Long sellerId;

    @NotNull
    private Status status;

    // Internal ID of the current winner
    private Long highestBidderId;

    // Helper for the frontend vibe
    private Boolean isExpired;

    /**
     * Helper method to determine expiration.
     * Often used during the mapping process in the Service layer.
     */
    public Boolean getIsExpired() {
        return auctionEndTime != null && LocalDateTime.now().isAfter(auctionEndTime);
    }
}