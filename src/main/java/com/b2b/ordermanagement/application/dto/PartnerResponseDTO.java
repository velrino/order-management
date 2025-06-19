package com.b2b.ordermanagement.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartnerResponseDTO(
        String id,
        String name,
        BigDecimal creditLimit,
        BigDecimal availableCredit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}