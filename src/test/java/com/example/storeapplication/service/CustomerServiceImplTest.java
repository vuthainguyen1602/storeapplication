package com.example.storeapplication.service;

import com.example.storeapplication.exception.InsufficientStockException;
import com.example.storeapplication.exception.ProductNotFoundException;
import com.example.storeapplication.domain.*;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.dto.BasketItemRequest;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductResponse;
import com.example.storeapplication.dto.ReceiptResponse;
import com.example.storeapplication.repository.BasketRepository;
import com.example.storeapplication.repository.DealRepository;
import com.example.storeapplication.repository.ProductRepository;
import com.example.storeapplication.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReceiptCalculationService receiptCalculationService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Product testProduct;
    private Basket testBasket;
    private BasketItem testBasketItem;
    private Deal testDeal;

    @BeforeEach
    void setUp() {
        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(100));
        testProduct.setCategory(Category.ELECTRONICS);
        testProduct.setStock(10);
        testProduct.setAvailable(true);

        // Setup test deal
        testDeal = Deal.builder()
                .id(1L)
                .product(testProduct)
                .description("Buy 2 Get 10% off")
                .buyQuantity(2)
                .discountPercentage(BigDecimal.TEN)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        // Setup test basket
        testBasket = new Basket();
        testBasket.setId(1L);
        testBasket.setSessionId("test-session");
        testBasket.setCreatedAt(LocalDateTime.now());

        // Setup test basket item
        testBasketItem = new BasketItem();
        testBasketItem.setId(1L);
        testBasketItem.setBasket(testBasket);
        testBasketItem.setProduct(testProduct);
        testBasketItem.setQuantity(1);
        testBasketItem.setUnitPrice(testProduct.getPrice());

        testBasket.getItems().add(testBasketItem);
    }

    @Test
    void addToBasket_NewBasket_ShouldCreateNewBasket() {
        // Arrange
        String sessionId = "new-session";
        BasketItemRequest request = new BasketItemRequest(1L, 2);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> {
            Basket b = invocation.getArgument(0);
            b.setId(2L);
            return b;
        });
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        String result = customerService.addToBasket(sessionId, request);

        // Assert
        assertEquals("Product added to basket successfully", result);
        verify(basketRepository, times(1)).save(any(Basket.class));
        verify(productRepository, times(1)).save(testProduct);
        assertEquals(8, testProduct.getStock()); // 10 - 2 = 8
    }

    @Test
    void addToBasket_ExistingBasket_ShouldUpdateBasket() {
        // Arrange
        String sessionId = "test-session";
        BasketItemRequest request = new BasketItemRequest(1L, 1);

        when(basketRepository.findBySessionId(sessionId)).thenReturn(Optional.of(testBasket));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(basketRepository.save(any(Basket.class))).thenReturn(testBasket);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        String result = customerService.addToBasket(sessionId, request);

        // Assert
        assertEquals("Product added to basket successfully", result);
        assertEquals(1, testBasketItem.getQuantity()); // Quantity increased from 1 to 2
        assertEquals(9, testProduct.getStock()); // 10 - 1 = 9
    }

    @Test
    void addToBasket_ProductNotFound_ShouldThrowException() {
        // Arrange
        String sessionId = "test-session";
        BasketItemRequest request = new BasketItemRequest(999L, 1);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
            () -> customerService.addToBasket(sessionId, request));
        verify(basketRepository, never()).save(any(Basket.class));
    }

    @Test
    void addToBasket_InsufficientStock_ShouldThrowException() {
        // Arrange
        String sessionId = "test-session";
        testProduct.setStock(1);
        BasketItemRequest request = new BasketItemRequest(1L, 2);

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
            () -> customerService.addToBasket(sessionId, request));
        verify(basketRepository, never()).save(any(Basket.class));
    }

    @Test
    void getProducts_ShouldReturnFilteredProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct), pageable, 1);
        
        when(productRepository.findAllWithFilter(
            eq(Category.ELECTRONICS), 
            any(), 
            any(), 
            eq(true), 
            any(Pageable.class))).thenReturn(productPage);

        // Act
        PageResponse<ProductResponse> response = customerService.getProducts(
            Category.ELECTRONICS, null, null, true, 0, 10, "name", "asc");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Product", response.getContent().get(0).getName());
        verify(productRepository, times(1))
            .findAllWithFilter(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getProducts_WithPriceRange_ShouldReturnFilteredProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price"));
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct), pageable, 1);
        
        when(productRepository.findAllWithFilter(
            any(), 
            eq(BigDecimal.valueOf(50)), 
            eq(BigDecimal.valueOf(150)), 
            any(), 
            any(Pageable.class))).thenReturn(productPage);

        // Act
        PageResponse<ProductResponse> response = customerService.getProducts(
            null, BigDecimal.valueOf(50), BigDecimal.valueOf(150), null, 0, 10, "price", "asc");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(productRepository, times(1))
            .findAllWithFilter(any(), any(), any(), any(), any(Pageable.class));
    }
}
