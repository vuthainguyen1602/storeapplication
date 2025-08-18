package com.example.storeapplication.service;

import com.example.storeapplication.exception.DealNotFoundException;
import com.example.storeapplication.exception.ProductNotFoundException;
import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.domain.Product;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.dto.DealCreateRequest;
import com.example.storeapplication.dto.ProductCreateRequest;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductResponse;
import com.example.storeapplication.repository.DealRepository;
import com.example.storeapplication.repository.ProductRepository;
import com.example.storeapplication.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Product testProduct;
    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(100));
        testProduct.setCategory(Category.ELECTRONICS);
        testProduct.setStock(10);
        testProduct.setAvailable(true);

        testDeal = Deal.builder()
                .id(1L)
                .product(testProduct)
                .description("Test Deal")
                .buyQuantity(2)
                .discountPercentage(BigDecimal.TEN)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();
    }

    @Test
    void createProduct_ShouldReturnProductResponse() {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest(
                "New Product", "New Description", BigDecimal.valueOf(200), Category.ELECTRONICS, 20);
        
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        // Act
        ProductResponse response = adminService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals("New Product", response.getName());
        assertEquals("New Description", response.getDescription());
        assertEquals(BigDecimal.valueOf(200), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void removeProduct_ShouldDeactivateProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        adminService.removeProduct(1L);

        // Assert
        assertFalse(testProduct.isAvailable());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void removeProduct_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> adminService.removeProduct(999L));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createDeal_ShouldReturnProductResponse() {
        DealCreateRequest request = DealCreateRequest.builder()
                .productId(1L)
                .description("Buy 1 Get 50% Off")
                .buyQuantity(1)
                .discountPercentage(BigDecimal.valueOf(50))
                .getQuantity(1)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> {
            Deal deal = invocation.getArgument(0);
            deal.setId(2L);
            return deal;
        });

        // Act
        ProductResponse response = adminService.createDeal(request);

        // Assert
        assertNotNull(response);
        assertEquals(testProduct.getName(), response.getName());
        verify(dealRepository, times(1)).save(any(Deal.class));
    }

    @Test
    void removeDeal_ShouldDeactivateDeal() {
        // Arrange
        when(dealRepository.findById(1L)).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // Act
        adminService.removeDeal(1L);

        // Assert
        assertFalse(testDeal.isActive());
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, times(1)).save(testDeal);
    }

    @Test
    void removeDeal_WhenDealNotFound_ShouldThrowException() {
        // Arrange
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DealNotFoundException.class, () -> adminService.removeDeal(999L));
        verify(dealRepository, times(1)).findById(999L);
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void getProducts_ShouldReturnPageOfProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct), pageable, 1);
        
        when(productRepository.findAllWithFilter(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

        // Act
        PageResponse<ProductResponse> response = adminService.getProducts(
                Category.ELECTRONICS, null, null, true, 0, 10, "name", "asc");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Product", response.getContent().get(0).getName());
        verify(productRepository, times(1))
                .findAllWithFilter(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getAllProducts_ShouldReturnPageOfProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct), pageable, 1);
        
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        // Act
        PageResponse<ProductResponse> response = adminService.getAllProducts(0, 10, "name", "asc");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Product", response.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getAllDeals_ShouldReturnPageOfActiveDeals() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Deal> dealPage = new PageImpl<>(Collections.singletonList(testDeal), pageable, 1);
        
        when(dealRepository.findActiveDeals(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(dealPage);

        // Act
        PageResponse<Deal> response = adminService.getAllDeals(0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Deal", response.getContent().get(0).getDescription());
        verify(dealRepository, times(1))
                .findActiveDeals(any(LocalDateTime.class), any(Pageable.class));
    }
}
