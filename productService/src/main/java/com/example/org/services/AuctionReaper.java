package com.example.org.services;


import com.example.org.dtos.ProductEvent;
import com.example.org.entities.OutboxEntity;
import com.example.org.entities.Product;
import com.example.org.enums.SaleType;
import com.example.org.enums.Status;
import com.example.org.repos.OutboxRepository;
import com.example.org.repos.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuctionReaper {

    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    @Transactional
    public void reapExpiredSessions(){

        List<Product> expiredAuctions = productRepository.findByStatusAndTypeAndAuctionEndTimeBefore(
                Status.ACTIVE,
                SaleType.AUCTION,
                LocalDateTime.now()
        );

        if(expiredAuctions.isEmpty()){
            return;
        }

        for (Product product : expiredAuctions) {

            boolean hasWinner = product.getHighestBidderId() != null;
            product.setStatus(hasWinner ? Status.SOLD : Status.INACTIVE);


            ProductEvent event = ProductEvent.builder()
                    .eventType(hasWinner ? "AUCTION_CLOSED" : "AUCTION_EXPIRED")
                    .productId(product.getId())
                    .title(product.getTitle())
                    .amount(product.getCurrentHighestBid())
                    .userId(product.getHighestBidderId()) // The winner!
                    .sellerId(product.getSellerId())
                    .timeStamp(LocalDateTime.now())
                    .build();


            try {
                    outboxRepository.save(OutboxEntity.builder()
                            .aggregateId(product.getId().toString())
                            .eventType(event.getEventType())
                            .payload(objectMapper.writeValueAsString(event))
                            .createdAt(LocalDateTime.now())
                            .processed(false)
                            .build());
                } catch (Exception e) {
                    log.error("Reaper: Failed to create outbox entry for product {}", product.getId());
                }

            productRepository.save(product);
            }



        }

    }

