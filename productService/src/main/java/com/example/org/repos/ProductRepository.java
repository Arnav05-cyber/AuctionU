package com.example.org.repos;

import com.example.org.entities.Product;
import com.example.org.enums.SaleType;
import com.example.org.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Used for general browsing
    List<Product> findByStatus(Status status);

    // Used for the Categories sidebar
    List<Product> findByCategoryAndStatus(String category, Status status);

    List<Product> findByCategory(String category);

    // THE MOST IMPORTANT ONES FOR YOUR NEW SPLIT:

    // 1. Marketplace Page: Finds only FIXED_PRICE items that are ACTIVE
    List<Product> findByTypeAndStatus(SaleType type, Status status);

    List<Product> findByType(SaleType type);
}