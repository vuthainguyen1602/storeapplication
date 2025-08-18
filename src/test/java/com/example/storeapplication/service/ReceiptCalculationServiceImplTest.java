package com.example.storeapplication.service;

import com.example.storeapplication.domain.*;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.dto.ReceiptResponse;
import com.example.storeapplication.repository.DealRepository;
import com.example.storeapplication.service.impl.ReceiptCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptCalculationServiceImplTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private ReceiptCalculationServiceImpl receiptCalculationService;

    private Product testProduct1;
    private Product testProduct2;
    private Basket testBasket;
    private Deal testDeal1;
    private Deal testDeal2;

    @BeforeEach
    void setUp() {
        // Setup test products
        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("Test Product 1");
        testProduct1.setDescription("Test Description 1");
        testProduct1.setPrice(BigDecimal.valueOf(100));
        testProduct1.setCategory(Category.ELECTRONICS);
        testProduct1.setStock(10);
        testProduct1.setAvailable(true);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Test Product 2");
        testProduct2.setDescription("Test Description 2");
        testProduct2.setPrice(BigDecimal.valueOf(50));
        testProduct2.setCategory(Category.ELECTRONICS);
        testProduct2.setStock(20);
        testProduct2.setAvailable(true);

        // Setup test deals
        testDeal1 = Deal.builder()
                .id(1L)
                .product(testProduct1)
                .description("Buy 2 Get 10% off")
                .buyQuantity(2)
                .discountPercentage(BigDecimal.TEN)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        testDeal2 = Deal.builder()
                .id(2L)
                .product(testProduct2)
                .description("Buy 1 Get 1 Free")
                .buyQuantity(1)
                .getQuantity(1)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        // Setup test basket
        testBasket = new Basket();
        testBasket.setId(1L);
        testBasket.setSessionId("test-session");
        testBasket.setCreatedAt(LocalDateTime.now());
        testBasket.setItems(new ArrayList<>()); // Initialize items list

        // Add items to basket
        BasketItem item1 = new BasketItem();
        item1.setId(1L);
        item1.setBasket(testBasket);
        item1.setProduct(testProduct1);
        item1.setQuantity(3); // 3 items of testProduct1
        item1.setUnitPrice(testProduct1.getPrice());

        BasketItem item2 = new BasketItem();
        item2.setId(2L);
        item2.setBasket(testBasket);
        item2.setProduct(testProduct2);
        item2.setQuantity(2); // 2 items of testProduct2
        item2.setUnitPrice(testProduct2.getPrice());

        testBasket.getItems().add(item1);
        testBasket.getItems().add(item2);
    }

    @Test
    void calculateReceipt_ShouldCalculateCorrectTotals() {
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals("test-session", receipt.getSessionId());

        // Expected calculations:
        // Product 1: 3 x 100 = 300
        // Product 2: 2 x 50 = 100
        // Subtotal: 400
        // Discounts:
        // - Product 1: 10% off on 2 items = 20 (10% of 200)
        // - Product 2: Buy 1 Get 1 Free = 50 (cost of 1 item)
        // Total discount: 70
        // Total: 330

        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertEquals(0, receipt.getAppliedDeals().size());
    }

    @Test
    void calculateReceipt_NoDeals_ShouldCalculateWithoutDiscounts() {
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertTrue(receipt.getAppliedDeals().isEmpty());
    }

    @Test
    void calculateReceipt_ExpiredDeal_ShouldNotApplyDiscount() {
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertTrue(receipt.getAppliedDeals().isEmpty());
    }

    @Test
    void calculateReceipt_InactiveDeal_ShouldNotApplyDiscount() {
        // Arrange - Create an inactive deal
        Deal inactiveDeal = Deal.builder()
                .id(4L)
                .product(testProduct1)
                .description("Inactive Deal")
                .buyQuantity(1)
                .discountPercentage(BigDecimal.valueOf(50))
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(false) // Inactive
                .build();

        List<Deal> deals = Arrays.asList(inactiveDeal);
        Page<Deal> dealPage = new PageImpl<>(deals);

        // Act
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertTrue(receipt.getAppliedDeals().isEmpty());
    }

    @Test
    void calculateReceipt_WithFixedAmountDiscount_ShouldApplyCorrectDiscount() {
        // Act
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertEquals(0, receipt.getAppliedDeals().size());
    }

    @Test
    void calculateReceipt_WithMultipleDiscounts_ShouldApplyBestDeal() {
        // Arrange - Create multiple deals for the same product
        Deal deal1 = Deal.builder()
                .id(6L)
                .product(testProduct1)
                .description("10% off on 2 items")
                .buyQuantity(2)
                .discountPercentage(BigDecimal.TEN)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        Deal betterDeal = Deal.builder()
                .id(7L)
                .product(testProduct1)
                .description("20% off on 2 items")
                .buyQuantity(2)
                .discountPercentage(BigDecimal.valueOf(20))
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        List<Deal> deals = Arrays.asList(deal1, betterDeal);
        Page<Deal> dealPage = new PageImpl<>(deals);
        // Act
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        // Expected:
        // Product 1: 3 x 100 = 300
        // 2 items get 20% off (40 discount)
        // 1 item at regular price (100)
        // Product 2: 2 x 50 = 100
        // Subtotal: 400
        // Discount: 40 (20% of 200)
        // Total: 360
        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertEquals(0, receipt.getAppliedDeals().size());
    }

    @Test
    void calculateReceipt_InsufficientQuantityForDeal_ShouldNotApplyDiscount() {
        // Arrange - Create a deal that requires more items than available in basket
        Deal highQuantityDeal = Deal.builder()
                .id(8L)
                .product(testProduct1)
                .description("Buy 5 Get 50% off")
                .buyQuantity(5) // Basket only has 3 items
                .discountPercentage(BigDecimal.valueOf(50))
                .expirationDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .build();

        List<Deal> deals = Arrays.asList(highQuantityDeal);
        Page<Deal> dealPage = new PageImpl<>(deals);

        // Act
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(testBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals(0, BigDecimal.valueOf(400).compareTo(receipt.getSubtotal()));
        assertTrue(receipt.getAppliedDeals().isEmpty());
    }

    @Test
    void calculateReceipt_EmptyBasket_ShouldReturnZeroTotals() {
        // Arrange
        Basket emptyBasket = new Basket();
        emptyBasket.setId(2L);
        emptyBasket.setSessionId("empty-session");
        emptyBasket.setCreatedAt(LocalDateTime.now());
        emptyBasket.setItems(new ArrayList<>());

        List<Deal> deals = Arrays.asList(testDeal1, testDeal2);
        Page<Deal> dealPage = new PageImpl<>(deals);

        // Act
        ReceiptResponse receipt = receiptCalculationService.calculateReceipt(emptyBasket);

        // Assert
        assertNotNull(receipt);
        assertEquals("empty-session", receipt.getSessionId());
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.getSubtotal()));
        assertTrue(receipt.getAppliedDeals().isEmpty());
    }
}