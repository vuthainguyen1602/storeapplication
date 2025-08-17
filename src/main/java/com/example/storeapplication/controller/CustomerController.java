package com.example.storeapplication.controller;

import com.example.storeapplication.dto.*;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/customer")
@AllArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ProductResponse> products = customerService.getProducts(
                category, minPrice, maxPrice, available, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(products);
    }

    @PostMapping("/basket/add")
    public ResponseEntity<String> addToBasket(
            @Valid @RequestBody BasketItemRequest request,
            HttpSession session) {

        String sessionId = session.getId();
        String result = customerService.addToBasket(sessionId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/basket/remove")
    public ResponseEntity<String> removeFromBasket(
            @Valid @RequestBody BasketItemRequest request,
            HttpSession session) {

        String sessionId = session.getId();
        String result = customerService.removeFromBasket(sessionId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/basket/receipt")
    public ResponseEntity<ReceiptResponse> getReceipt(HttpSession session) {
        String sessionId = session.getId();
        ReceiptResponse receipt = customerService.calculateReceipt(sessionId);
        return ResponseEntity.ok(receipt);
    }

}
