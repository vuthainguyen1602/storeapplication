package com.example.storeapplication.controller;

import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.dto.DealCreateRequest;
import com.example.storeapplication.dto.PageResponse;
import com.example.storeapplication.dto.ProductCreateRequest;
import com.example.storeapplication.dto.ProductResponse;
import com.example.storeapplication.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void testCreateProduct() throws Exception {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest(
                "Wireless Mouse",
                "Ergonomic wireless mouse",
                BigDecimal.valueOf(29.99),
                com.example.storeapplication.enums.Category.ELECTRONICS,
                100
        );

        ProductResponse mockResponse = new ProductResponse(
                1L,
                "Wireless Mouse",
                "Ergonomic wireless mouse",
                BigDecimal.valueOf(29.99),
                com.example.storeapplication.enums.Category.ELECTRONICS,
                100,
                true,
                LocalDateTime.now()
        );

        when(adminService.createProduct(any(ProductCreateRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.description").value("Ergonomic wireless mouse"))
                .andExpect(jsonPath("$.price").value(29.99));

        verify(adminService).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    void testRemoveProduct() throws Exception {
        // Arrange
        Long productId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/admin/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string("Product removed successfully"));

        verify(adminService).removeProduct(eq(productId));
    }

    @Test
    void testGetAllProducts() throws Exception {
        // Arrange
        ProductResponse productResponse = new ProductResponse(
                1L,
                "Keyboard",
                "Mechanical keyboard",
                BigDecimal.valueOf(50.0),
                com.example.storeapplication.enums.Category.ELECTRONICS,
                50,
                true,
                LocalDateTime.now()
        );

        PageResponse<ProductResponse> mockPage = new PageResponse<>(
                List.of(productResponse),
                1, 0, 10, 1
        );

        when(adminService.getAllProducts(0, 10, "id", "asc"))
                .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/admin/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Keyboard"))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(0));

        verify(adminService).getAllProducts(0, 10, "id", "asc");
    }

    @Test
    void testCreateDeal() throws Exception {
        // Arrange
        DealCreateRequest request = DealCreateRequest.builder()
                .productId(1L)
                .description("Buy 1 Get 50% Off")
                .buyQuantity(1)
                .discountPercentage(BigDecimal.valueOf(50))
                .getQuantity(1)
                .expirationDate(LocalDateTime.now().plusDays(7))
                .build();

        ProductResponse mockResponse = new ProductResponse(
                1L,
                "Laptop",
                "Gaming Laptop",
                BigDecimal.valueOf(1200.0),
                com.example.storeapplication.enums.Category.ELECTRONICS,
                10,
                true,
                LocalDateTime.now()
        );

        when(adminService.createDeal(any(DealCreateRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/admin/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1200.0));

        verify(adminService).createDeal(any(DealCreateRequest.class));
    }

    @Test
    void testGetAllDeals() throws Exception {
        // Arrange
        Deal deal = new Deal();
        deal.setId(1L);
        deal.setDescription("Summer Sale");

        PageResponse<Deal> mockPage = new PageResponse<>(
                List.of(deal),
                1, 0, 10, 1
        );

        when(adminService.getAllDeals(0, 10)).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/admin/deals")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Summer Sale"))
                .andExpect(jsonPath("$.totalElements").value(10));

        verify(adminService).getAllDeals(0, 10);
    }

    @Test
    void testRemoveDeal() throws Exception {
        // Arrange
        Long dealId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/admin/deals/{id}", dealId))
                .andExpect(status().isOk())
                .andExpect(content().string("Deal removed successfully"));

        verify(adminService).removeDeal(eq(dealId));
    }
}