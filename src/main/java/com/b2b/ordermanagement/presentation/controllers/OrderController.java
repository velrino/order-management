package com.b2b.ordermanagement.presentation.controllers;

import com.b2b.ordermanagement.application.dto.CreateOrderDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.application.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}