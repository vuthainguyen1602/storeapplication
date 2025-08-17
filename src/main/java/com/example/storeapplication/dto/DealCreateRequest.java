package com.example.storeapplication.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealCreateRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Deal description is required")
    private String description;

    @Min(value = 1, message = "Buy quantity must be at least 1")
    private int buyQuantity;

    @DecimalMin(value = "0.01", message = "Discount percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.01", message = "Discount amount must be greater than 0")
    private BigDecimal discountAmount;

    @Min(value = 1, message = "Get quantity must be at least 1")
    private Integer getQuantity;

    private LocalDateTime expirationDate;
}