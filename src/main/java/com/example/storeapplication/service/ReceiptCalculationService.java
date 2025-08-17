package com.example.storeapplication.service;

import com.example.storeapplication.domain.Basket;
import com.example.storeapplication.domain.BasketItem;
import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.dto.ReceiptResponse;

import java.math.BigDecimal;

public interface ReceiptCalculationService {
    ReceiptResponse calculateReceipt(Basket basket);
    BigDecimal calculateDiscount(BasketItem item, Deal deal);
}
