package com.example.org.dtos;

import com.example.org.enums.SaleType;
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


    private LocalDateTime auctionEndTime;

    private LocalDateTime createdAt;

    @NotNull
    private String sellerId;



    @NotNull
    private Status status;

    // Internal ID of the current winner
    private String highestBidderId;

    // Helper for the frontend vibe
    private Boolean isExpired;


    @PositiveOrZero
    private Integer quantity; // To show "10 left in stock"

    @PositiveOrZero
    private BigDecimal buyItNowPrice; // The fixed price for retail

    @NotNull
    private SaleType saleType;

    /**
     * Helper method to determine expiration.
     * Often used during the mapping process in the Service layer.
     */
    public Boolean getIsExpired() {
        // If it's not an auction, it can't be "expired" in the bidding sense
        if (this.saleType != SaleType.AUCTION) {
            return false;
        }
        return auctionEndTime != null && LocalDateTime.now().isAfter(auctionEndTime);
    }
}