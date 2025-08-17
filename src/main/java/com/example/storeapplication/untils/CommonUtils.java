package com.example.storeapplication.untils;

import com.example.storeapplication.domain.Product;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductResponse;
import org.springframework.data.domain.Page;

public class CommonUtils {
    public static ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStock(),
                product.isAvailable(),
                product.getCreatedAt()
        );
    }

    public PageResponse<ProductResponse> mapToPageResponse(Page<Product> productPage) {
        return new PageResponse<>(
                productPage.getContent().stream()
                        .map(CommonUtils::mapToProductResponse)
                        .toList(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }
}
