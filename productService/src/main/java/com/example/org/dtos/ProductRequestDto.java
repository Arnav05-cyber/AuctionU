package com.example.org.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequestDto {

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;
    private String category;

    // Type of sale: AUCTION or FIXED_PRICE
    @NotNull(message = "Sale type is mandatory")
    private com.example.org.enums.SaleType type;

    // --- Auction Specific Fields ---
    // These should not be @NotNull anymore because they are null for FIXED_PRICE
    private BigDecimal startingPrice;
    private LocalDateTime auctionEndTime;

    // --- Marketplace Specific Fields ---
    private Integer quantity; // How many items are in stock
    private BigDecimal buyItNowPrice; // The fixed price

    @NotBlank(message = "Seller ID is mandatory")
    private String sellerId;
}