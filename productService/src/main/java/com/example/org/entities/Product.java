package com.example.org.entities;


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
    @Column(name = "product_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "starting_price", nullable = false)
    private BigDecimal startingPrice;

    @Column(name = "current_highest_bid")
    private BigDecimal currentHighestBid;

    @Column(name = "auction_end_time", nullable = false)
    private LocalDateTime auctionEndTime;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @PrePersist
    protected void onCreate() {
        if(this.currentHighestBid == null) {
            this.currentHighestBid = this.startingPrice;
        }
    }

}
