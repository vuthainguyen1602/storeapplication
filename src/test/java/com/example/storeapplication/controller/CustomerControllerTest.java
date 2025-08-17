package com.example.storeapplication.controller;

import com.example.storeapplication.dto.*;
import com.example.storeapplication.enums.Category;
import com.example.storeapplication.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockHttpSession mockSession;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        mockSession = new MockHttpSession();
        mockSession.setAttribute("sessionId", "test-session-123");
    }

    @Test
    void testGetProducts_WithoutFilters() throws Exception {
        // Arrange
        ProductResponse productResponse = new ProductResponse(
                1L,
                "Laptop",
                "Gaming Laptop",
                BigDecimal.valueOf(1200.0),
                Category.ELECTRONICS,
                10,
                true,
                LocalDateTime.now()
        );

        PageResponse<ProductResponse> mockPage = new PageResponse<>(
                List.of(productResponse),
                1, 0, 10, 1
        );

        when(customerService.getProducts(
                eq(null), eq(null), eq(null), eq(null),
                eq(0), eq(10), eq("id"), eq("asc")))
                .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/customer/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[0].price").value(1200.0))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(0));

        verify(customerService).getProducts(
                eq(null), eq(null), eq(null), eq(null),
                eq(0), eq(10), eq("id"), eq("asc"));
    }

    @Test
    void testGetProducts_WithFilters() throws Exception {
        // Arrange
        ProductResponse productResponse = new ProductResponse(
                2L,
                "Smartphone",
                "Android Phone",
                BigDecimal.valueOf(800.0),
                Category.ELECTRONICS,
                5,
                true,
                LocalDateTime.now()
        );

        PageResponse<ProductResponse> mockPage = new PageResponse<>(
                List.of(productResponse),
                1, 0, 5, 1
        );

        when(customerService.getProducts(
                eq(Category.ELECTRONICS),
                eq(BigDecimal.valueOf(500)),
                eq(BigDecimal.valueOf(1000)),
                eq(true),
                eq(0), eq(5), eq("name"), eq("desc")))
                .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/customer/products")
                        .param("category", "ELECTRONICS")
                        .param("minPrice", "500")
                        .param("maxPrice", "1000")
                        .param("available", "true")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Smartphone"))
                .andExpect(jsonPath("$.content[0].category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.size").value(0));

        verify(customerService).getProducts(
                eq(Category.ELECTRONICS),
                eq(BigDecimal.valueOf(500)),
                eq(BigDecimal.valueOf(1000)),
                eq(true),
                eq(0), eq(5), eq("name"), eq("desc"));
    }

    @Test
    void testAddToBasket() throws Exception {
        // Arrange
        BasketItemRequest request = new BasketItemRequest(1L, 2);
        String expectedResult = "Item added to basket successfully";

        when(customerService.addToBasket(anyString(), any(BasketItemRequest.class)))
                .thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/customer/basket/add")
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));

        verify(customerService).addToBasket(anyString(), any(BasketItemRequest.class));
    }

    @Test
    void testRemoveFromBasket() throws Exception {
        // Arrange
        BasketItemRequest request = new BasketItemRequest(1L, 1);
        String expectedResult = "Item removed from basket successfully";

        when(customerService.removeFromBasket(anyString(), any(BasketItemRequest.class)))
                .thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/customer/basket/remove")
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));

        verify(customerService).removeFromBasket(anyString(), any(BasketItemRequest.class));
    }

    @Test
    void testGetReceipt() throws Exception {
        // Arrange
        List<ReceiptResponse.ReceiptItem> receiptItems = List.of(
                ReceiptResponse.ReceiptItem.builder()
                        .productId(1L)
                        .productName("Laptop")
                        .unitPrice(BigDecimal.valueOf(1200.0))
                        .quantity(1)
                        .totalPrice(BigDecimal.valueOf(1200.0))
                        .build(),
                ReceiptResponse.ReceiptItem.builder()
                        .productId(2L)
                        .productName("Mouse")
                        .unitPrice(BigDecimal.valueOf(25.0))
                        .quantity(2)
                        .totalPrice(BigDecimal.valueOf(50.0))
                        .build()
        );

        List<ReceiptResponse.AppliedDeal> appliedDeals = List.of(
                ReceiptResponse.AppliedDeal.builder()
                        .description("Buy 1 Get 50% Off")
                        .discountAmount(BigDecimal.valueOf(25.0))
                        .build(),
                ReceiptResponse.AppliedDeal.builder()
                        .description("Summer Sale 10%")
                        .discountAmount(BigDecimal.valueOf(100.0))
                        .build()
        );

        ReceiptResponse mockReceipt = ReceiptResponse.builder()
                .sessionId("test-session-123")
                .items(receiptItems)
                .appliedDeals(appliedDeals)
                .subtotal(BigDecimal.valueOf(1250.0))
                .totalDiscount(BigDecimal.valueOf(125.0))
                .totalPrice(BigDecimal.valueOf(1125.0))
                .generatedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();

        when(customerService.calculateReceipt(anyString())).thenReturn(mockReceipt);

        // Act & Assert
        mockMvc.perform(get("/customer/basket/receipt")
                        .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("test-session-123"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1200.0))
                .andExpect(jsonPath("$.items[0].totalPrice").value(1200.0))
                .andExpect(jsonPath("$.items[1].productName").value("Mouse"))
                .andExpect(jsonPath("$.items[1].quantity").value(2))
                .andExpect(jsonPath("$.items[1].unitPrice").value(25.0))
                .andExpect(jsonPath("$.items[1].totalPrice").value(50.0))
                .andExpect(jsonPath("$.appliedDeals").isArray())
                .andExpect(jsonPath("$.appliedDeals").isNotEmpty())
                .andExpect(jsonPath("$.appliedDeals[0].description").value("Buy 1 Get 50% Off"))
                .andExpect(jsonPath("$.appliedDeals[0].discountAmount").value(25.0))
                .andExpect(jsonPath("$.appliedDeals[1].description").value("Summer Sale 10%"))
                .andExpect(jsonPath("$.appliedDeals[1].discountAmount").value(100.0))
                .andExpect(jsonPath("$.subtotal").value(1250.0))
                .andExpect(jsonPath("$.totalDiscount").value(125.0))
                .andExpect(jsonPath("$.totalPrice").value(1125.0))
                .andExpect(jsonPath("$.generatedAt").exists());

        verify(customerService).calculateReceipt(anyString());
    }

    @Test
    void testAddToBasket_WithInvalidRequest() throws Exception {
        // Arrange - Invalid request with null productId
        BasketItemRequest invalidRequest = new BasketItemRequest(null, 1);

        // Act & Assert - Expecting validation error
        mockMvc.perform(post("/customer/basket/add")
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveFromBasket_WithInvalidQuantity() throws Exception {
        // Arrange - Invalid request with negative quantity
        BasketItemRequest invalidRequest = new BasketItemRequest(1L, -1);

        // Act & Assert - Expecting validation error
        mockMvc.perform(post("/customer/basket/remove")
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProducts_WithPagination() throws Exception {
        // Arrange
        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "Product1", "Description1", BigDecimal.valueOf(10.0),
                        Category.ELECTRONICS, 5, true, LocalDateTime.now()),
                new ProductResponse(2L, "Product2", "Description2", BigDecimal.valueOf(20.0),
                        Category.ELECTRONICS, 3, true, LocalDateTime.now())
        );

        PageResponse<ProductResponse> mockPage = new PageResponse<>(
                products, 2, 1, 2, 5 // totalElements=5, currentPage=1, pageSize=2, totalPages=3
        );

        when(customerService.getProducts(
                eq(null), eq(null), eq(null), eq(null),
                eq(1), eq(2), eq("price"), eq("asc")))
                .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/customer/products")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sortBy", "price")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalPages").value(5));

        verify(customerService).getProducts(
                eq(null), eq(null), eq(null), eq(null),
                eq(1), eq(2), eq("price"), eq("asc"));
    }
}