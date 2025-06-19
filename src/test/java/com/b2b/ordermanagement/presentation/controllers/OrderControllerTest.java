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

    @Nested
    @DisplayName("GET /api/v1/orders - Get Orders with Pagination and Filters")
    class GetOrdersWithFiltersTests {

        @Test
        @DisplayName("Should return paginated orders with default parameters")
        void getOrders_WithDefaultParams_ShouldReturnPaginatedOrders() throws Exception {
            // Arrange
            OrderResponseDTO order1 = new OrderResponseDTO("ORDER001", "PARTNER001", OrderStatus.PENDING,
                    BigDecimal.valueOf(200), LocalDateTime.now(), LocalDateTime.now(), List.of());
            OrderResponseDTO order2 = new OrderResponseDTO("ORDER002", "PARTNER002", OrderStatus.APPROVED,
                    BigDecimal.valueOf(300), LocalDateTime.now(), LocalDateTime.now(), List.of());

            Page<OrderResponseDTO> page = new PageImpl<>(List.of(order1, order2));
            PagedResponse<OrderResponseDTO> pagedResponse = PagedResponse.of(page);

            when(orderService.getFilteredOrders(any(OrderFilterParams.class), any(Pageable.class)))
                    .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").isArray())
                    .andExpect(jsonPath("$.records", hasSize(2)))
                    .andExpect(jsonPath("$.records[0].id").value("ORDER001"))
                    .andExpect(jsonPath("$.records[1].id").value("ORDER002"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.pages").value(1));
        }

        @Test
        @DisplayName("Should filter orders by partner ID")
        void getOrders_WithPartnerId_ShouldReturnFilteredOrders() throws Exception {
            OrderResponseDTO order = new OrderResponseDTO("ORDER001", "PARTNER001", OrderStatus.PENDING,
                    BigDecimal.valueOf(200), LocalDateTime.now(), LocalDateTime.now(), List.of());

            Page<OrderResponseDTO> page = new PageImpl<>(List.of(order));

            when(orderService.getFilteredOrders(any(OrderFilterParams.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/orders")
                            .param("partnerId", "PARTNER001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").isArray())
                    .andExpect(jsonPath("$.records[0].partnerId").value("PARTNER001"));

            verify(orderService).getFilteredOrders(argThat(filter ->
                    "PARTNER001".equals(((OrderFilterDTO) filter).getPartnerId())), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter orders by status")
        void getOrders_WithStatus_ShouldReturnFilteredOrders() throws Exception {
            OrderResponseDTO order = new OrderResponseDTO("ORDER001", "PARTNER001", OrderStatus.APPROVED,
                    BigDecimal.valueOf(200), LocalDateTime.now(), LocalDateTime.now(), List.of());

            Page<OrderResponseDTO> page = new PageImpl<>(List.of(order));

            when(orderService.getFilteredOrders(any(OrderFilterParams.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/orders")
                            .param("status", "APPROVED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records[0].status").value("APPROVED"));

            verify(orderService).getFilteredOrders(argThat(filter ->
                    OrderStatus.APPROVED.equals(((OrderFilterDTO) filter).getStatus())), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter orders by date range")
        void getOrders_WithDateRange_ShouldReturnFilteredOrders() throws Exception {
            OrderResponseDTO order = new OrderResponseDTO("ORDER001", "PARTNER001", OrderStatus.PENDING,
                    BigDecimal.valueOf(200), LocalDateTime.now(), LocalDateTime.now(), List.of());

            Page<OrderResponseDTO> page = new PageImpl<>(List.of(order));

            when(orderService.getFilteredOrders(any(OrderFilterParams.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/orders")
                            .param("startDate", "2025-01-01T00:00:00")
                            .param("endDate", "2025-12-31T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").isArray());

            verify(orderService).getFilteredOrders(argThat(filter -> {
                OrderFilterDTO dto = (OrderFilterDTO) filter;
                return dto.getStartDate() != null && dto.getEndDate() != null;
            }), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/orders/{orderId}/approve - Approve Order")
    class ApproveOrderTests {

        @Test
        @DisplayName("Should approve order successfully")
        void approveOrder_WithValidId_ShouldReturnApprovedOrder() throws Exception {
            String orderId = "ORDER001";
            OrderResponseDTO responseDTO = new OrderResponseDTO(
                    orderId,
                    "PARTNER001",
                    OrderStatus.APPROVED,
                    BigDecimal.valueOf(200),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    List.of()
            );

            when(orderService.approveOrder(orderId)).thenReturn(responseDTO);

            mockMvc.perform(put("/api/v1/orders/{orderId}/approve", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void approveOrder_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String orderId = "INVALID_ORDER";

            when(orderService.approveOrder(orderId))
                    .thenThrow(new ResourceNotFoundException("Order not found: " + orderId));

            mockMvc.perform(put("/api/v1/orders/{orderId}/approve", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/orders/{orderId}/cancel - Cancel Order")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order successfully")
        void cancelOrder_WithValidId_ShouldReturnCancelledOrder() throws Exception {
            String orderId = "ORDER001";
            OrderResponseDTO responseDTO = new OrderResponseDTO(
                    orderId,
                    "PARTNER001",
                    OrderStatus.CANCELLED,
                    BigDecimal.valueOf(200),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    List.of()
            );

            when(orderService.cancelOrder(orderId)).thenReturn(responseDTO);

            mockMvc.perform(put("/api/v1/orders/{orderId}/cancel", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }
    }
}