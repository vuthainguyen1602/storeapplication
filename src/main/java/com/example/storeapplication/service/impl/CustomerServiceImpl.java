package com.example.storeapplication.service.impl;

import com.example.storeapplication.domain.Basket;
import com.example.storeapplication.domain.Product;
import com.example.storeapplication.dto.*;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.exception.InsufficientStockException;
import com.example.storeapplication.exception.ProductNotFoundException;
import com.example.storeapplication.repository.BasketRepository;
import com.example.storeapplication.repository.ProductRepository;
import com.example.storeapplication.service.CustomerService;
import com.example.storeapplication.service.ReceiptCalculationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

@Service
@AllArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final BasketRepository basketRepository;

    private final ProductRepository productRepository;

    private final ReentrantLock stockLock = new ReentrantLock();

    private final ReceiptCalculationService receiptCalculationService;

    /**
     * @param sessionId
     * @param request
     * @return
     */
    @Override
    public String addToBasket(String sessionId, BasketItemRequest request) {
        stockLock.lock();
        try {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

            if (!product.isAvailable()) {
                throw new ProductNotFoundException("Product is not available");
            }

            if (product.getStock() < request.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock. Available: %d, Requested: %d",
                                product.getStock(), request.getQuantity())
                );
            }

            // Decrement stock atomically
            boolean success = false;
            int attempts = 0;
            while (!success && attempts < 3) {
                success = product.decrementStock(request.getQuantity());
                attempts++;
                if (!success) {
                    Thread.sleep(10); // Brief wait before retry
                }
            }

            if (!success) {
                throw new InsufficientStockException("Failed to reserve stock after multiple attempts");
            }

            Basket basket = basketRepository.findBySessionId(sessionId)
                    .orElse(new Basket(sessionId));

            basket.addItem(product, request.getQuantity());
            basketRepository.save(basket);
            productRepository.save(product);

            return "Product added to basket successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation was interrupted", e);
        } finally {
            stockLock.unlock();
        }
    }

    /**
     * @param sessionId
     * @param request
     * @return
     */
    @Override
    public String removeFromBasket(String sessionId, BasketItemRequest request) {
        stockLock.lock();
        try {
            Basket basket = basketRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Basket not found for session: " + sessionId));

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

            boolean removed = basket.removeItem(request.getProductId(), request.getQuantity());
            if (removed) {
                // Return stock
                product.incrementStock(request.getQuantity());
                basketRepository.save(basket);
                productRepository.save(product);
                return "Product removed from basket successfully";
            } else {
                throw new RuntimeException("Product not found in basket or insufficient quantity");
            }

        } finally {
            stockLock.unlock();
        }
    }

    /**
     * @param sessionId
     * @return
     */
    @Override
    @Transactional
    public ReceiptResponse calculateReceipt(String sessionId) {
        Basket basket = basketRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Basket not found for session: " + sessionId));

        return receiptCalculationService.calculateReceipt(basket);
    }


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
    public PageResponse<ProductResponse> getProducts(Category category, BigDecimal minPrice,
                                                     BigDecimal maxPrice, Boolean available,
                                                     int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAllWithFilter(category, minPrice, maxPrice, available, pageable);

        return mapToPageResponse(productPage);

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
