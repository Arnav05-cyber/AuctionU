package com.example.org.services;


import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;

import java.util.List;

public interface ProductService {

    public ProductResponseDto createProduct(ProductRequestDto productRequestDto);

    List<ProductResponseDto> getallProducts();

    ProductResponseDto getProductById(Long productId);

    List<ProductResponseDto> getProductsByCategory(String category);

    void deleteProductById(Long id);

}
