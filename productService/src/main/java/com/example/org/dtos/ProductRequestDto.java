package com.example.org.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

    @NotNull
    @Positive(message = "Starting price must be a positive value")
    private BigDecimal startingPrice;

    @Future(message = "Auction end time must be in the future")
    private LocalDateTime auctionEndTime;

    @NotBlank
    private Long sellerId;

}
