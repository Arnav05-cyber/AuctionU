package com.example.org.controllers;

import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;
import com.example.org.entities.Product;
import com.example.org.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService  productService;

    @GetMapping("/marketplace")
    public ResponseEntity<List<ProductResponseDto>> getMarketPlaceProducts() {
        return ResponseEntity.ok(productService.getMarketplaceItems());
    }

    @GetMapping("/auction")
    public ResponseEntity<List<ProductResponseDto>> getAuctionProducts() {
        return ResponseEntity.ok(productService.getAuctionItems());
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto, @RequestHeader("X-User-Id") String sellerId) {
        requestDto.setSellerId(sellerId);
        return ResponseEntity.ok(productService.createProduct(requestDto));
    }

}
