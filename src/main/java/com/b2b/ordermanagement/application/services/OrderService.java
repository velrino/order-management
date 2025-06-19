package com.b2b.ordermanagement.application.services;

import com.b2b.ordermanagement.application.dto.CreateOrderDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.entities.OrderItem;
import com.b2b.ordermanagement.infrastructure.repositories.OrderRepository;
import com.b2b.ordermanagement.shared.exceptions.ResourceNotFoundException;
import com.b2b.ordermanagement.shared.mappers.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public OrderResponseDTO createOrder(CreateOrderDTO createOrderDTO) {
        logger.info("Creating order for partner: {}", createOrderDTO.partnerId());


        // Create order items
        List<OrderItem> orderItems = createOrderDTO.items().stream()
                .map(itemDto -> new OrderItem(itemDto.productId(), itemDto.quantity(), itemDto.unitPrice()))
                .toList();

        // Create order
        Order order = new Order(createOrderDTO.partnerId(), orderItems);

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully: {}", savedOrder.getId());



        return orderMapper.toResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        return orderMapper.toResponseDTO(order);
    }
}