package com.example.org.mappers;

import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;
import com.example.org.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "currentHighestBid", source = "startingPrice")
    @Mapping(target = "highestBidderId", ignore = true) // New field from our entity update
    Product toEntity(ProductRequestDto productRequestDto);

    @Mapping(target = "isExpired", source = "product", qualifiedByName = "calculateExpiry")
    ProductResponseDto toDto(Product product);

    @Named("calculateExpiry")
    default boolean calculateExpiry(Product product) {

        if (product.getType() == com.example.org.enums.SaleType.FIXED_PRICE) {
            return false;
        }
        if (product.getAuctionEndTime() == null) {
            return false;
        }
        return java.time.LocalDateTime.now().isAfter(product.getAuctionEndTime());
    }
}
