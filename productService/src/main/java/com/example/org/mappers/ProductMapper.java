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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "currentHighestBid",  source = "startingPrice")
    Product toEntity(ProductRequestDto productRequestDto);

    @Mapping(target = "isExpired", source = "product", qualifiedByName = "calculateExpiry")
    ProductResponseDto toDto(Product product);




    @Named("calculateExpiry")
    default boolean calculateExpiry(Product product) {
        if(product.getAuctionEndTime() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(product.getAuctionEndTime());
    }

}
