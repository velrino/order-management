package com.b2b.ordermanagement.application.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long id,
        String productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}