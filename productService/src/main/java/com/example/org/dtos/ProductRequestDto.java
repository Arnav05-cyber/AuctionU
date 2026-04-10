package com.example.org.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "Starting price is mandatory")
    @Positive(message = "Starting price must be a positive value")
    private BigDecimal startingPrice;

    @NotNull(message = "Auction end time is mandatory")
    @Future(message = "Auction end time must be in the future")
    private LocalDateTime auctionEndTime;

    @NotNull(message = "Seller ID is mandatory") // Fixed from @NotBlank
    private Long sellerId;
}