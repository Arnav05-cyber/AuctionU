package com.example.org.services;


import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    public ProductResponseDto createProduct(ProductRequestDto productRequestDto);

    List<ProductResponseDto> getallProducts();

    ProductResponseDto getProductById(Long productId);

    List<ProductResponseDto> getProductsByCategory(String category);

    List<ProductResponseDto> getMarketplaceItems();
    List<ProductResponseDto> getAuctionItems();


    void purchaseProduct(Long productId, Integer quantity, String buyerId);
    void placeBid(Long productId, BigDecimal amount, String bidderId);

    void deleteProductById(Long id);


}
