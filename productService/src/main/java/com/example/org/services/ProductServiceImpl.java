package com.example.org.services;


import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;
import com.example.org.entities.Product;
import com.example.org.enums.SaleType;
import com.example.org.enums.Status;
import com.example.org.mappers.ProductMapper;
import com.example.org.repos.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(Status.DELETED);
        productRepository.save(product);
    }

    @Override
    public List<ProductResponseDto> getMarketplaceItems() {
        return productRepository.findByTypeAndStatus(SaleType.FIXED_PRICE, Status.ACTIVE).stream().map(productMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void purchaseProduct(Long productId, Integer quantity, String buyerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if(!product.getType().equals(SaleType.FIXED_PRICE)) {
            throw new RuntimeException("Product is not available for direct purchase");
        }

        if(product.getQuantity() < quantity) {
        throw new RuntimeException("Product quantity less than quantity");
        }

        product.setQuantity(product.getQuantity() - quantity);

        if (product.getQuantity() == 0) {
            product.setStatus(Status.SOLD);
        }

        productRepository.save(product);
    }

    @Override
    public List<ProductResponseDto> getAuctionItems() {
        return productRepository.findByType(SaleType.AUCTION).stream().filter(product -> product.getStatus().equals(Status.ACTIVE)).map(productMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void placeBid(Long productId, BigDecimal amount, String bidderId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if(!product.getType().equals(SaleType.AUCTION)) {
            throw new RuntimeException("Product is not available for bidding");
        }

        if(product.getAuctionEndTime().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Auction has already ended");
        }

        if(amount .compareTo(product.getCurrentHighestBid()) <= 0) {
            throw new RuntimeException("Bid amount must be higher than current highest bid");
        }

        if(!product.getStatus().equals(Status.ACTIVE)) {
            throw new RuntimeException("Product is not active for bidding");
        }

        product.setCurrentHighestBid(amount);
        product.setHighestBidderId(bidderId);

        productRepository.save(product);
    }
}
