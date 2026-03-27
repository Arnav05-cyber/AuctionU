package com.example.org.services;


import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;
import com.example.org.entities.Product;
import com.example.org.enums.Status;
import com.example.org.mappers.ProductMapper;
import com.example.org.repos.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository  productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        Product product =  productMapper.toEntity(productRequestDto);
        product.setStatus(Status.ACTIVE);
        Product savedProduct =  productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public List<ProductResponseDto> getallProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().filter(product -> product.getStatus().equals(Status.ACTIVE)).map(productMapper::toDto).toList();
    }

    @Override
    public ProductResponseDto getProductById(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getStatus().equals(Status.ACTIVE)) // Only return if Active
                .map(productMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Product not found or is inactive"));
    }

    @Override
    public List<ProductResponseDto> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        return products.stream().filter(product -> product.getStatus().equals(Status.ACTIVE)).map(productMapper::toDto).toList();
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }
}
