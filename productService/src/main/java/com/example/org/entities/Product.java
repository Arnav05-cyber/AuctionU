package com.example.org.entities;


import com.example.org.enums.SaleType;
import com.example.org.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    // Prices
    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;
    private BigDecimal buyItNowPrice; // Changed from Double

    // Times
    private LocalDateTime auctionEndTime; // Remove nullable=false
    private LocalDateTime createdAt;

    // Identity (Matches Gateway/Auth UUIDs)
    private String sellerId; // Changed from Long
    private String highestBidderId; // Added this

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private SaleType type;

    private Integer quantity; // For Marketplace items

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if(this.currentHighestBid == null && this.startingPrice != null) {
            this.currentHighestBid = this.startingPrice;
        }
    }
}