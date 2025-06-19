package com.b2b.ordermanagement.shared.mappers;

import com.b2b.ordermanagement.application.dto.OrderItemResponseDTO;
import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.entities.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(this::toItemResponseDTO)
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getPartnerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemDTOs
        );
    }

    public OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        return new OrderItemResponseDTO(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}