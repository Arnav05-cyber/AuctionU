package com.example.org.repos;

import com.example.org.entities.Product;
import com.example.org.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByStatus(Status status);


    List<Product> findByCategory(String category);


    List<Product> findByStatusAndCategory(Status status, String category);
}