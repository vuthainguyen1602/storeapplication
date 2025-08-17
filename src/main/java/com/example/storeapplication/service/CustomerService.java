package com.example.storeapplication.service;

import com.example.storeapplication.dto.*;
import com.example.storeapplication.enums.Category;

import java.math.BigDecimal;

public interface CustomerService {
    String addToBasket(String sessionId, BasketItemRequest request);
    String removeFromBasket(String sessionId, BasketItemRequest request);
    ReceiptResponse calculateReceipt(String sessionId);
    PageResponse<ProductResponse> getProducts(
            Category category, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean available, int page, int size, String sortBy, String sortDir);


}
