package com.example.storeapplication.controller;


import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.dto.DealCreateRequest;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductCreateRequest;
import com.example.storeapplication.dto.ProductResponse;
import com.example.storeapplication.service.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse product = adminService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> removeProduct(@PathVariable Long productId) {
        adminService.removeProduct(productId);
        return ResponseEntity.ok("Product removed successfully");
    }

    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ProductResponse> products = adminService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/deals")
    public ResponseEntity<ProductResponse> createDeal(@Valid @RequestBody DealCreateRequest request) {
        ProductResponse product = adminService.createDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/deals")
    public ResponseEntity<PageResponse<Deal>> getAllDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<Deal> deals = adminService.getAllDeals(page, size);
        return ResponseEntity.ok(deals);
    }

    @DeleteMapping("/deals/{dealId}")
    public ResponseEntity<String> removeDeal(@PathVariable Long dealId) {
        adminService.removeDeal(dealId);
        return ResponseEntity.ok("Deal removed successfully");
    }
}
