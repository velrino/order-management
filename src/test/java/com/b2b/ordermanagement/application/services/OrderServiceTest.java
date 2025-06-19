package com.b2b.ordermanagement.application.services;

import com.b2b.ordermanagement.application.dto.CreateOrderDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.application.interfaces.OrderFilterParams;
import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.entities.OrderItem;
import com.b2b.ordermanagement.domain.entities.Partner;
import com.b2b.ordermanagement.domain.enums.OrderStatus;
import com.b2b.ordermanagement.infrastructure.repositories.OrderRepository;
import com.b2b.ordermanagement.shared.exceptions.BusinessException;
import com.b2b.ordermanagement.shared.exceptions.ResourceNotFoundException;
import com.b2b.ordermanagement.shared.mappers.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderService TDD Tests")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PartnerService partnerService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private Partner mockPartner;
    private Order mockOrder;
    private CreateOrderDTO validCreateOrderDTO;
    private OrderResponseDTO mockOrderResponseDTO;

    @BeforeEach
    void setUp() {
        mockPartner = createMockPartner();
        mockOrder = createMockOrder();
        validCreateOrderDTO = createValidOrderDTO();
        mockOrderResponseDTO = createMockOrderResponseDTO();
    }

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully when all conditions are met")
        void shouldCreateOrderSuccessfully() {
            CreateOrderDTO orderDTO = mock(CreateOrderDTO.class);
            when(orderDTO.partnerId()).thenReturn("PARTNER001");
            when(orderDTO.items()).thenReturn(List.of());

            when(partnerService.getPartnerEntityById("PARTNER001")).thenReturn(mockPartner);
            when(mockPartner.hasAvailableCredit(any(BigDecimal.class))).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            OrderResponseDTO result = orderService.createOrder(orderDTO);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(mockOrder.getId());
            verify(partnerService).getPartnerEntityById("PARTNER001");
            verify(orderRepository).save(any(Order.class));
            verify(notificationService).simulateMessageSend(eq("order.created"), anyString());
        }

        @Test
        @DisplayName("Should throw BusinessException when partner has insufficient credit")
        void shouldThrowBusinessExceptionWhenInsufficientCredit() {
            CreateOrderDTO orderDTO = mock(CreateOrderDTO.class);
            when(orderDTO.partnerId()).thenReturn("PARTNER001");
            when(orderDTO.items()).thenReturn(List.of());

            when(partnerService.getPartnerEntityById("PARTNER001")).thenReturn(mockPartner);
            when(mockPartner.hasAvailableCredit(any(BigDecimal.class))).thenReturn(false);
            when(mockPartner.getId()).thenReturn("PARTNER001");

            assertThatThrownBy(() -> orderService.createOrder(orderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient credit available for partner: PARTNER001");

            verify(orderRepository, never()).save(any(Order.class));
            verify(notificationService, never()).simulateMessageSend(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw BusinessException when partner service throws exception")
        void shouldThrowBusinessExceptionWhenPartnerServiceFails() {
            CreateOrderDTO orderDTO = mock(CreateOrderDTO.class);
            when(orderDTO.partnerId()).thenReturn("PARTNER001");
            when(orderDTO.items()).thenReturn(List.of());

            when(partnerService.getPartnerEntityById("PARTNER001"))
                    .thenThrow(new RuntimeException("Partner service error"));

            assertThatThrownBy(() -> orderService.createOrder(orderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Error creating order: Partner service error");

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when order repository throws exception")
        void shouldThrowBusinessExceptionWhenRepositoryFails() {
            CreateOrderDTO orderDTO = mock(CreateOrderDTO.class);
            when(orderDTO.partnerId()).thenReturn("PARTNER001");
            when(orderDTO.items()).thenReturn(List.of());

            when(partnerService.getPartnerEntityById("PARTNER001")).thenReturn(mockPartner);
            when(mockPartner.hasAvailableCredit(any(BigDecimal.class))).thenReturn(true);
            when(orderRepository.save(any(Order.class)))
                    .thenThrow(new RuntimeException("Database error"));

            assertThatThrownBy(() -> orderService.createOrder(orderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Error creating order: Database error");
        }

        @Test
        @DisplayName("Should handle empty items list gracefully")
        void shouldHandleEmptyItemsList() {
            CreateOrderDTO emptyItemsDTO = mock(CreateOrderDTO.class);
            when(emptyItemsDTO.partnerId()).thenReturn("PARTNER001");
            when(emptyItemsDTO.items()).thenReturn(List.of());

            when(partnerService.getPartnerEntityById("PARTNER001")).thenReturn(mockPartner);
            when(mockPartner.hasAvailableCredit(any(BigDecimal.class))).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            OrderResponseDTO result = orderService.createOrder(emptyItemsDTO);

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Get Order By ID Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when found")
        void shouldReturnOrderWhenFound() {
            String orderId = "order-123";
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(orderMapper.toResponseDTO(mockOrder)).thenReturn(mockOrderResponseDTO);

            OrderResponseDTO result = orderService.getOrderById(orderId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(mockOrder.getId());
            verify(orderRepository).findById(orderId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void shouldThrowResourceNotFoundExceptionWhenOrderNotFound() {
            String orderId = "non-existent-order";
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(orderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found: " + orderId);
        }
    }

    @Nested
    @DisplayName("Get Filtered Orders Tests")
    class GetFilteredOrdersTests {

        private OrderFilterParams mockFilters;
        private Pageable mockPageable;
        private Page<Order> mockOrderPage;

        @BeforeEach
        void setUp() {
            mockFilters = mock(OrderFilterParams.class);
            mockPageable = PageRequest.of(0, 10);
            mockOrderPage = new PageImpl<>(List.of(mockOrder));
        }

        @Test
        @DisplayName("Should return filtered orders with all filters applied")
        void shouldReturnFilteredOrdersWithAllFilters() {
            when(mockFilters.hasPartnerId()).thenReturn(true);
            when(mockFilters.hasStatus()).thenReturn(true);
            when(mockFilters.hasDateRange()).thenReturn(true);
            when(mockFilters.getPartnerId()).thenReturn("PARTNER001");
            when(mockFilters.getStatus()).thenReturn(OrderStatus.PENDING);
            when(mockFilters.getStartDate()).thenReturn(LocalDateTime.now().minusDays(7));
            when(mockFilters.getEndDate()).thenReturn(LocalDateTime.now());

            when(orderRepository.findByPartnerIdAndStatusAndCreatedAtBetween(
                    anyString(), any(OrderStatus.class), any(LocalDateTime.class),
                    any(LocalDateTime.class), any(Pageable.class)
            )).thenReturn(mockOrderPage);

            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            Page<OrderResponseDTO> result = orderService.getFilteredOrders(mockFilters, mockPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(orderRepository).findByPartnerIdAndStatusAndCreatedAtBetween(
                    anyString(), any(OrderStatus.class), any(LocalDateTime.class),
                    any(LocalDateTime.class), any(Pageable.class)
            );
        }

        @Test
        @DisplayName("Should return all orders when no filters applied")
        void shouldReturnAllOrdersWhenNoFiltersApplied() {
            when(mockFilters.hasPartnerId()).thenReturn(false);
            when(mockFilters.hasStatus()).thenReturn(false);
            when(mockFilters.hasDateRange()).thenReturn(false);
            when(orderRepository.findAll(any(Pageable.class))).thenReturn(mockOrderPage);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            Page<OrderResponseDTO> result = orderService.getFilteredOrders(mockFilters, mockPageable);

            assertThat(result).isNotNull();
            verify(orderRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle empty results gracefully")
        void shouldHandleEmptyResultsGracefully() {
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(mockFilters.hasPartnerId()).thenReturn(false);
            when(mockFilters.hasStatus()).thenReturn(false);
            when(mockFilters.hasDateRange()).thenReturn(false);
            when(orderRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<OrderResponseDTO> result = orderService.getFilteredOrders(mockFilters, mockPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Approve Order Tests")
    class ApproveOrderTests {

        @Test
        @DisplayName("Should approve order successfully when conditions are met")
        void shouldApproveOrderSuccessfully() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeApproved()).thenReturn(true);
            when(mockOrder.getStatus()).thenReturn(OrderStatus.PENDING);
            when(mockOrder.getPartnerId()).thenReturn("PARTNER001");
            when(mockOrder.getTotalAmount()).thenReturn(BigDecimal.valueOf(100.00));
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            OrderResponseDTO result = orderService.approveOrder(orderId);

            assertThat(result).isNotNull();
            verify(partnerService).debitCredit("PARTNER001", BigDecimal.valueOf(100.00));
            verify(mockOrder).updateStatus(OrderStatus.APPROVED);
            verify(orderRepository).save(mockOrder);
            verify(notificationService).simulateMessageSend(eq("order.status.changed"), anyString());
        }

        @Test
        @DisplayName("Should throw BusinessException when order not found")
        void shouldThrowBusinessExceptionWhenOrderNotFound() {
            String orderId = "non-existent-order";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.approveOrder(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Error creating order: Order not found: " + orderId);

            verify(partnerService, never()).debitCredit(anyString(), any(BigDecimal.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when order cannot be approved")
        void shouldThrowBusinessExceptionWhenOrderCannotBeApproved() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeApproved()).thenReturn(false);
            when(mockOrder.getStatus()).thenReturn(OrderStatus.CANCELLED);

            assertThatThrownBy(() -> orderService.approveOrder(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Order cannot be approved in current status: CANCELLED");

            verify(partnerService, never()).debitCredit(anyString(), any(BigDecimal.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when partner service fails")
        void shouldThrowBusinessExceptionWhenPartnerServiceFails() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeApproved()).thenReturn(true);
            when(mockOrder.getPartnerId()).thenReturn("PARTNER001");
            when(mockOrder.getTotalAmount()).thenReturn(BigDecimal.valueOf(100.00));
            doThrow(new RuntimeException("Partner service error"))
                    .when(partnerService).debitCredit(anyString(), any(BigDecimal.class));

            assertThatThrownBy(() -> orderService.approveOrder(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Error creating order: Partner service error");
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel pending order successfully without credit restoration")
        void shouldCancelPendingOrderSuccessfully() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeCancelled()).thenReturn(true);
            when(mockOrder.getStatus()).thenReturn(OrderStatus.PENDING);
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            OrderResponseDTO result = orderService.cancelOrder(orderId);

            assertThat(result).isNotNull();
            verify(mockOrder).updateStatus(OrderStatus.CANCELLED);
            verify(orderRepository).save(mockOrder);
            verify(partnerService, never()).restoreCredit(anyString(), any(BigDecimal.class));
            verify(notificationService).simulateMessageSend(eq("order.status.changed"), anyString());
        }

        @Test
        @DisplayName("Should cancel approved order and restore credit")
        void shouldCancelApprovedOrderAndRestoreCredit() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeCancelled()).thenReturn(true);
            when(mockOrder.getStatus()).thenReturn(OrderStatus.APPROVED);
            when(mockOrder.getPartnerId()).thenReturn("PARTNER001");
            when(mockOrder.getTotalAmount()).thenReturn(BigDecimal.valueOf(100.00));
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
            when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mockOrderResponseDTO);

            OrderResponseDTO result = orderService.cancelOrder(orderId);

            assertThat(result).isNotNull();
            verify(partnerService).restoreCredit("PARTNER001", BigDecimal.valueOf(100.00));
            verify(mockOrder).updateStatus(OrderStatus.CANCELLED);
            verify(orderRepository).save(mockOrder);
        }

        @Test
        @DisplayName("Should throw BusinessException when order not found")
        void shouldThrowBusinessExceptionWhenOrderNotFound() {
            String orderId = "non-existent-order";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Error creating order: Order not found: " + orderId);

            verify(partnerService, never()).restoreCredit(anyString(), any(BigDecimal.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when order cannot be cancelled")
        void shouldThrowBusinessExceptionWhenOrderCannotBeCancelled() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeCancelled()).thenReturn(false);
            when(mockOrder.getStatus()).thenReturn(OrderStatus.DELIVERED);

            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Order cannot be cancelled in current status: DELIVERED");

            verify(partnerService, never()).restoreCredit(anyString(), any(BigDecimal.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when partner service fails during credit restoration")
        void shouldThrowBusinessExceptionWhenPartnerServiceFailsDuringCreditRestoration() {
            String orderId = "order-123";
            when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(mockOrder));
            when(mockOrder.canBeCancelled()).thenReturn(true);
            when(mockOrder.getStatus()).thenReturn(OrderStatus.APPROVED);
            when(mockOrder.getPartnerId()).thenReturn("PARTNER001");
            when(mockOrder.getTotalAmount()).thenReturn(BigDecimal.valueOf(100.00));
            doThrow(new RuntimeException("Partner service error"))
                    .when(partnerService).restoreCredit(anyString(), any(BigDecimal.class));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Error creating order: Partner service error");
        }
    }

    private Partner createMockPartner() {
        Partner partner = mock(Partner.class);
        when(partner.getId()).thenReturn("PARTNER001");
        return partner;
    }

    private Order createMockOrder() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn("order-123");
        when(order.getPartnerId()).thenReturn("PARTNER001");
        when(order.getTotalAmount()).thenReturn(BigDecimal.valueOf(100.00));
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(order.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(order.getItems()).thenReturn(List.of(mock(OrderItem.class)));
        return order;
    }

    private CreateOrderDTO createValidOrderDTO() {
        CreateOrderDTO dto = mock(CreateOrderDTO.class);
        when(dto.partnerId()).thenReturn("PARTNER001");

        when(dto.items()).thenReturn(List.of());
        return dto;
    }

    private OrderResponseDTO createMockOrderResponseDTO() {
        OrderResponseDTO dto = mock(OrderResponseDTO.class);
        when(dto.id()).thenReturn("order-123");
        when(dto.partnerId()).thenReturn("PARTNER001");
        when(dto.status()).thenReturn(OrderStatus.PENDING);
        when(dto.totalAmount()).thenReturn(BigDecimal.valueOf(100.00));
        when(dto.items()).thenReturn(List.of());
        when(dto.createdAt()).thenReturn(LocalDateTime.now());
        when(dto.updatedAt()).thenReturn(LocalDateTime.now());
        return dto;
    }
}