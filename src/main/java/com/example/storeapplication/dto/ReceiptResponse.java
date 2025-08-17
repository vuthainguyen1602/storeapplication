package com.example.storeapplication.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptResponse {
    private String sessionId;
    private List<ReceiptItem> items;
    private List<AppliedDeal> appliedDeals;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalPrice;
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiptItem {
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal totalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedDeal {
        private String description;
        private BigDecimal discountAmount;
    }
}