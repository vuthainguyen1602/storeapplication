package com.example.storeapplication.service;

import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.dto.DealCreateRequest;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductCreateRequest;
import com.example.storeapplication.dto.ProductResponse;
import com.example.storeapplication.enums.Category;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {
    PageResponse<ProductResponse> getProducts(Category category, BigDecimal minPrice, BigDecimal maxPrice,
                                              Boolean available, int page, int size, String sortBy, String sortDir);

    PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    ProductResponse createProduct(ProductCreateRequest request);

   void removeProduct(Long productId);

    ProductResponse createDeal(DealCreateRequest request);

   void removeDeal(Long dealId);

    PageResponse<Deal> getAllDeals(int page, int size);

}

