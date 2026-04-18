package com.example.org.services;


import com.example.org.dtos.ProductEvent;
import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;
import com.example.org.entities.OutboxEntity;
import com.example.org.entities.Product;
import com.example.org.enums.SaleType;
import com.example.org.enums.Status;
import com.example.org.mappers.ProductMapper;
import com.example.org.repos.OutboxRepository;
import com.example.org.repos.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, OutboxRepository outboxRepository,
                              ObjectMapper objectMapper, SimpMessagingTemplate simpMessagingTemplate
    , StringRedisTemplate stringRedisTemplate) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        Product product = productMapper.toEntity(productRequestDto);
        product.setStatus(Status.ACTIVE);
        Product savedProduct = productRepository.save(product);
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

        if (product.getSellerId().equals(buyerId)) {
            throw new RuntimeException("Seller cannot purchase their own product");
        }

        if (!product.getType().equals(SaleType.FIXED_PRICE)) {
            throw new RuntimeException("Product is not available for direct purchase");
        }

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Product quantity less than quantity");
        }

        product.setQuantity(product.getQuantity() - quantity);

        if (product.getQuantity() == 0) {
            product.setStatus(Status.SOLD);
        }

        productRepository.save(product);

        ProductEvent productEvent = ProductEvent.builder()
                .eventType("PURCHASE")
                .productId(product.getId())
                .title(product.getTitle())
                .amount(product.getBuyItNowPrice().multiply(BigDecimal.valueOf(quantity)))
                .userId(buyerId)
                .sellerId(product.getSellerId())
                .timeStamp(java.time.LocalDateTime.now())
                .build();

        try {
            outboxRepository.save(OutboxEntity.builder()
                    .aggregateId(productId.toString())
                    .eventType(productEvent.getEventType()) // Use the event we just built
                    .payload(objectMapper.writeValueAsString(productEvent)) // Use the event we just built
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing event for outbox", e);
        }
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

        if (product.getSellerId().equals(bidderId)) {
            throw new RuntimeException("Seller cannot bid on their own product");
        }

        if (!product.getType().equals(SaleType.AUCTION)) {
            throw new RuntimeException("Product is not available for bidding");
        }

        if (product.getAuctionEndTime().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Auction has already ended");
        }

        if (amount.compareTo(product.getCurrentHighestBid()) <= 0) {
            throw new RuntimeException("Bid amount must be higher than current highest bid");
        }

        if (!product.getStatus().equals(Status.ACTIVE)) {
            throw new RuntimeException("Product is not active for bidding");
        }

        product.setCurrentHighestBid(amount);
        product.setHighestBidderId(bidderId);

        productRepository.save(product);


        ProductEvent productEvent = ProductEvent.builder()
                .eventType("BID_PLACED")
                .productId(product.getId())
                .title(product.getTitle())
                .amount(amount)
                .userId(bidderId)
                .sellerId(product.getSellerId())
                .timeStamp(java.time.LocalDateTime.now())
                .build();


        try {
            outboxRepository.save(OutboxEntity.builder()
                    .aggregateId(productId.toString())
                    .eventType(productEvent.getEventType()) // Use the event we just built
                    .payload(objectMapper.writeValueAsString(productEvent)) // Use the event we just built
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing event for outbox", e);
        }


        String redisMessage = productId + ":" + "New bid: " + amount;
        stringRedisTemplate.convertAndSend("auction-updates", redisMessage);
    }
}
