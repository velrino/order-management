package com.b2b.ordermanagement.presentation.controllers;

import com.b2b.ordermanagement.application.dto.*;
import com.b2b.ordermanagement.application.interfaces.OrderFilterParams;
import com.b2b.ordermanagement.application.services.OrderService;
import com.b2b.ordermanagement.domain.enums.OrderStatus;
import com.b2b.ordermanagement.shared.exceptions.BusinessException;
import com.b2b.ordermanagement.shared.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/orders - Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with valid data")
        void createOrder_WithValidData_ShouldReturnCreated() throws Exception {
            OrderItemDTO itemDTO = new OrderItemDTO("PROD001", 2, BigDecimal.valueOf(100));
            CreateOrderDTO createOrderDTO = new CreateOrderDTO("PARTNER001", List.of(itemDTO));

            OrderResponseDTO responseDTO = new OrderResponseDTO(
                    "ORDER001",
                    "PARTNER001",
                    OrderStatus.PENDING,
                    BigDecimal.valueOf(200),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    List.of()
            );

            when(orderService.createOrder(any(CreateOrderDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("ORDER001"))
                    .andExpect(jsonPath("$.partnerId").value("PARTNER001"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.totalAmount").value(200));
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void createOrder_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            CreateOrderDTO invalidDTO = new CreateOrderDTO("", List.of());

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when item validation fails")
        void createOrder_WithInvalidItemData_ShouldReturnBadRequest() throws Exception {
            OrderItemDTO invalidItem = new OrderItemDTO("PROD001", 0, BigDecimal.ZERO);
            CreateOrderDTO createOrderDTO = new CreateOrderDTO("PARTNER001", List.of(invalidItem));

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors['items[0].quantity']").exists())
                    .andExpect(jsonPath("$.fieldErrors['items[0].unitPrice']").exists());
        }

        @Test
        @DisplayName("Should return 404 when partner not found")
        void createOrder_WithNonExistentPartner_ShouldReturnNotFound() throws Exception {
            OrderItemDTO itemDTO = new OrderItemDTO("PROD001", 2, BigDecimal.valueOf(100));
            CreateOrderDTO createOrderDTO = new CreateOrderDTO("INVALID_PARTNER", List.of(itemDTO));

            when(orderService.createOrder(any(CreateOrderDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Partner not found: INVALID_PARTNER"));

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Partner not found: INVALID_PARTNER"));
        }

        @Test
        @DisplayName("Should return 400 when insufficient credit")
        void createOrder_WithInsufficientCredit_ShouldReturnBadRequest() throws Exception {
            OrderItemDTO itemDTO = new OrderItemDTO("PROD001", 100, BigDecimal.valueOf(1000));
            CreateOrderDTO createOrderDTO = new CreateOrderDTO("PARTNER001", List.of(itemDTO));

            when(orderService.createOrder(any(CreateOrderDTO.class)))
                    .thenThrow(new BusinessException("Insufficient credit available for partner: PARTNER001"));

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                    .andExpect(jsonPath("$.message").value("Insufficient credit available for partner: PARTNER001"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId} - Get Order by ID")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when ID exists")
        void getOrderById_WithValidId_ShouldReturnOrder() throws Exception {
            String orderId = "ORDER001";
            OrderResponseDTO responseDTO = new OrderResponseDTO(
                    orderId,
                    "PARTNER001",
                    OrderStatus.PENDING,
                    BigDecimal.valueOf(200),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    List.of()
            );

            when(orderService.getOrderById(orderId)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.partnerId").value("PARTNER001"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void getOrderById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String orderId = "INVALID_ORDER";

            when(orderService.getOrderById(orderId))
                    .thenThrow(new ResourceNotFoundException("Order not found: " + orderId));

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Order not found: " + orderId));
        }
    }
}