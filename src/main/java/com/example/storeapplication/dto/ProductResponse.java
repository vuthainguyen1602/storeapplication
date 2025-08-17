package com.example.storeapplication.dto;

import com.example.storeapplication.enums.Category;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private int stock;
    private boolean available;
    private LocalDateTime createdAt;
}

