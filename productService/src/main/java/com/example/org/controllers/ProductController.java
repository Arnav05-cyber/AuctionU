package com.example.org.controllers;

import com.example.org.dtos.ProductRequestDto;
import com.example.org.dtos.ProductResponseDto;
import com.example.org.entities.Product;
import com.example.org.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto, @RequestHeader("X-User-Id") String sellerId) {
        requestDto.setSellerId(sellerId);
        return ResponseEntity.ok(productService.createProduct(requestDto));
    }

    @PostMapping("/{id}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long id, @RequestBody Map<String, Object> body, @RequestHeader("X-User-Id") String bidderId) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        productService.placeBid(id, amount, bidderId);
        return ResponseEntity.ok(Map.of("message", "Bid placed successfully"));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchaseProduct(@PathVariable Long id, @RequestBody Map<String, Object> body, @RequestHeader("X-User-Id") String buyerId) {
        Integer quantity = Integer.parseInt(body.get("quantity").toString());
        productService.purchaseProduct(id, quantity, buyerId);
        return ResponseEntity.ok(Map.of("message", "Purchase successful"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        // Fetch the product to verify ownership
        ProductResponseDto product = productService.getProductById(id);
        if (!product.getSellerId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only the seller can delete this product"));
        }
        productService.deleteProductById(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

}
