package com.b2b.ordermanagement.application.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.b2b.ordermanagement.application.dto.CreateOrderDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.application.interfaces.OrderFilterParams;
import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.entities.OrderItem;
import com.b2b.ordermanagement.domain.enums.OrderStatus;
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

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findAll() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getFilteredOrders(OrderFilterParams filters, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "created_at")
            );
        }

        Page<Order> orders;

        if (filters.hasPartnerId() && filters.hasStatus() && filters.hasDateRange()) {
            // Partner + Status + DateRange
            orders = orderRepository.findByPartnerIdAndStatusAndCreatedAtBetween(
                    filters.getPartnerId(), filters.getStatus(),
                    filters.getStartDate(), filters.getEndDate(), pageable);

        } else if (filters.hasPartnerId() && filters.hasDateRange()) {
            // Partner + DateRange
            orders = orderRepository.findByPartnerIdAndCreatedAtBetween(
                    filters.getPartnerId(), filters.getStartDate(), filters.getEndDate(), pageable);

        } else if (filters.hasPartnerId() && filters.hasStatus()) {
            // Partner + Status
            orders = orderRepository.findByPartnerIdAndStatus(
                    filters.getPartnerId(), filters.getStatus(), pageable);

        } else if (filters.hasStatus() && filters.hasDateRange()) {
            // Status + DateRange
            orders = orderRepository.findByStatusAndCreatedAtBetween(
                    filters.getStatus(), filters.getStartDate(), filters.getEndDate(), pageable);

        } else if (filters.hasPartnerId()) {
            // Apenas Partner
            orders = orderRepository.findByPartnerIdOrderByCreatedAtDesc(filters.getPartnerId(), pageable);

        } else if (filters.hasStatus()) {
            // Apenas Status
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(filters.getStatus(), pageable);

        } else if (filters.hasDateRange()) {
            // Apenas DateRange
            orders = orderRepository.findByCreatedAtBetween(
                    filters.getStartDate(), filters.getEndDate(), pageable);

        } else {
            // Sem filtros - todos os pedidos
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(orderMapper::toResponseDTO);
    }
}