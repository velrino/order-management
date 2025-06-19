package com.b2b.ordermanagement.application.services;

import com.b2b.ordermanagement.domain.entities.Partner;
import com.b2b.ordermanagement.domain.enums.OrderStatus;
import com.b2b.ordermanagement.shared.exceptions.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.b2b.ordermanagement.application.dto.CreateOrderDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.application.interfaces.OrderFilterParams;
import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.entities.OrderItem;
import com.b2b.ordermanagement.infrastructure.repositories.OrderRepository;
import com.b2b.ordermanagement.shared.exceptions.ResourceNotFoundException;
import com.b2b.ordermanagement.shared.mappers.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PartnerService partnerService;

    public OrderService(OrderRepository orderRepository,
                        PartnerService partnerService,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.partnerService = partnerService;
        this.orderMapper = orderMapper;
    }

    public OrderResponseDTO createOrder(CreateOrderDTO createOrderDTO) {
        try {
            Partner partner = partnerService.getPartnerEntityById(createOrderDTO.partnerId());

            logger.info("Creating order for partner: {}", createOrderDTO.partnerId());

            // Create order items
            List<OrderItem> orderItems = createOrderDTO.items().stream()
                    .map(itemDto -> new OrderItem(itemDto.productId(), itemDto.quantity(), itemDto.unitPrice()))
                    .toList();

            // Create order
            Order order = new Order(createOrderDTO.partnerId(), orderItems);

            // Check credit availability
            if (!partner.hasAvailableCredit(order.getTotalAmount())) {
                throw new BusinessException("Insufficient credit available for partner: " + partner.getId());
            }

            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully: {}", savedOrder.getId());

            return orderMapper.toResponseDTO(savedOrder);

        } catch (Exception e) {
            logger.error("Unexpected error creating order for partner: {}", createOrderDTO.partnerId(), e);
            throw new BusinessException("Error creating order: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        return orderMapper.toResponseDTO(order);
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
            // Partner, Status and DateRange
            orders = orderRepository.findByPartnerIdAndStatusAndCreatedAtBetween(
                    filters.getPartnerId(), filters.getStatus(),
                    filters.getStartDate(), filters.getEndDate(), pageable);

        } else if (filters.hasPartnerId() && filters.hasDateRange()) {
            // Partner and DateRange
            orders = orderRepository.findByPartnerIdAndCreatedAtBetween(
                    filters.getPartnerId(), filters.getStartDate(), filters.getEndDate(), pageable);

        } else if (filters.hasPartnerId() && filters.hasStatus()) {
            // Partner and Status
            orders = orderRepository.findByPartnerIdAndStatus(
                    filters.getPartnerId(), filters.getStatus(), pageable);

        } else if (filters.hasStatus() && filters.hasDateRange()) {
            // Only and DateRange
            orders = orderRepository.findByStatusAndCreatedAtBetween(
                    filters.getStatus(), filters.getStartDate(), filters.getEndDate(), pageable);

        } else if (filters.hasPartnerId()) {
            // Only Partner
            orders = orderRepository.findByPartnerIdOrderByCreatedAtDesc(filters.getPartnerId(), pageable);

        } else if (filters.hasStatus()) {
            // Only Status
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(filters.getStatus(), pageable);

        } else if (filters.hasDateRange()) {
            // Only DateRange
            orders = orderRepository.findByCreatedAtBetween(
                    filters.getStartDate(), filters.getEndDate(), pageable);

        } else {
            // All orders without filter
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(orderMapper::toResponseDTO);
    }

    public OrderResponseDTO approveOrder(String orderId) {
        try {
            logger.info("Approving order: {}", orderId);

            Order order = orderRepository.findByIdWithLock(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

            if (!order.canBeApproved()) {
                throw new BusinessException("Order cannot be approved in current status: " + order.getStatus());
            }

            // Debit partner credit
            partnerService.debitCredit(order.getPartnerId(), order.getTotalAmount());

            OrderStatus previousStatus = order.getStatus();
            order.updateStatus(OrderStatus.APPROVED);

            Order savedOrder = orderRepository.save(order);
            logger.info("Order approved successfully: {}", orderId);

            return orderMapper.toResponseDTO(savedOrder);
        } catch (Exception e) {
            logger.error("Unexpected error approve order: {}", orderId, e);
            throw new BusinessException("Error creating order: " + e.getMessage());
        }
    }

    public OrderResponseDTO cancelOrder(String orderId) {
        try {
            logger.info("Cancelling order: {}", orderId);

            Order order = orderRepository.findByIdWithLock(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

            if (!order.canBeCancelled()) {
                throw new BusinessException("Order cannot be cancelled in current status: " + order.getStatus());
            }

            // If order was approved, restore partner credit
            if (order.getStatus() == OrderStatus.APPROVED ||
                    order.getStatus() == OrderStatus.PROCESSING ||
                    order.getStatus() == OrderStatus.SHIPPED) {
                partnerService.restoreCredit(order.getPartnerId(), order.getTotalAmount());
            }

            OrderStatus previousStatus = order.getStatus();
            order.updateStatus(OrderStatus.CANCELLED);

            Order savedOrder = orderRepository.save(order);
            logger.info("Order cancelled successfully: {}", orderId);

            return orderMapper.toResponseDTO(savedOrder);
        } catch (Exception e) {
            logger.error("Unexpected error approve order: {}", orderId, e);
            throw new BusinessException("Error creating order: " + e.getMessage());
        }
    }
}