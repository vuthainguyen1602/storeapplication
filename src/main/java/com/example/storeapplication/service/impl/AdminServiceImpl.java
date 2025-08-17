package com.example.storeapplication.service.impl;

import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.domain.Product;
import com.example.storeapplication.dto.DealCreateRequest;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductCreateRequest;
import com.example.storeapplication.dto.ProductResponse;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.exception.DealNotFoundException;
import com.example.storeapplication.exception.ProductNotFoundException;
import com.example.storeapplication.repository.DealRepository;
import com.example.storeapplication.repository.ProductRepository;
import com.example.storeapplication.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ProductRepository productRepository;

    private final DealRepository dealRepository;
    /**
     * @param category
     * @param minPrice
     * @param maxPrice
     * @param available
     * @param page
     * @param size
     * @param sortBy
     * @param sortDir
     * @return
     */
    @Override
    public PageResponse<ProductResponse> getProducts(Category category, BigDecimal minPrice, BigDecimal maxPrice, Boolean available, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAllWithFilter(category, minPrice, maxPrice, available, pageable);

        return mapToPageResponse(productPage);
    }

    /**
     * @param page
     * @param size
     * @param sortBy
     * @param sortDir
     * @return
     */
    @Override
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        return mapToPageResponse(productPage);
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory(),
                request.getStock()
        );

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    public void removeProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Soft delete - mark as unavailable
        product.setAvailable(false);
        productRepository.save(product);
    }

    /**
     * @param request
     * @return
     */
    @Override
    public ProductResponse createDeal(DealCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: "
                        + request.getProductId()));

        Deal deal = new Deal(
                product,
                request.getDescription(),
                request.getBuyQuantity(),
                request.getDiscountPercentage(),
                request.getExpirationDate()
        );

        if (request.getDiscountAmount() != null) {
            deal.setDiscountAmount(request.getDiscountAmount());
        }

        if (request.getGetQuantity() != null) {
            deal.setGetQuantity(request.getGetQuantity());
        }

        dealRepository.save(deal);
        return mapToProductResponse(product);
    }

    /**
     * @param dealId
     */
    @Override
    public void removeDeal(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new DealNotFoundException("Deal not found with id: " + dealId));

        deal.setActive(false);
        dealRepository.save(deal);
    }

    /**
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResponse<Deal> getAllDeals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Deal> dealPage = dealRepository.findActiveDeals(LocalDateTime.now(), pageable);

        return new PageResponse<>(
                dealPage.getContent(),
                dealPage.getNumber(),
                dealPage.getSize(),
                dealPage.getTotalElements(),
                dealPage.getTotalPages()
        );
    }


    private ProductResponse mapToProductResponse(Product product) {
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

    private PageResponse<ProductResponse> mapToPageResponse(Page<Product> productPage) {
        return new PageResponse<>(
                productPage.getContent().stream()
                        .map(this::mapToProductResponse)
                        .toList(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }
}
