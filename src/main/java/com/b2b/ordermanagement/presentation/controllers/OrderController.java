package com.b2b.ordermanagement.presentation.controllers;

import com.b2b.ordermanagement.application.dto.OrderFilterDTO;
import com.b2b.ordermanagement.application.dto.PagedResponse;
import com.b2b.ordermanagement.application.interfaces.OrderFilterParams;
import org.springframework.data.domain.Page;
import com.b2b.ordermanagement.application.dto.CreateOrderDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.application.services.OrderService;
import com.b2b.ordermanagement.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order for a partner")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        OrderResponseDTO order = orderService.createOrder(createOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its unique identifier")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }


    @GetMapping
    @Operation(summary = "Get orders with filters", description = "Retrieves orders filtered by various criteria")
    public ResponseEntity<PagedResponse<OrderResponseDTO>> getOrders(
            @Parameter(description = "Partner ID") @RequestParam(required = false) String partnerId,
            @Parameter(description = "Order status") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Start date (ISO format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        OrderFilterParams filters = new OrderFilterDTO(partnerId, status, startDate, endDate);
        Page<OrderResponseDTO> orders = orderService.getFilteredOrders(filters, pageable);

        return ResponseEntity.ok(PagedResponse.of(orders));
    }

    @PutMapping("/{orderId}/approve")
    @Operation(summary = "Approve order", description = "Approves a pending order and debits partner credit")
    public ResponseEntity<OrderResponseDTO> approveOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        OrderResponseDTO order = orderService.approveOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order and restores partner credit if applicable")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        OrderResponseDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }
}