package com.b2b.ordermanagement.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemDTO(
        @NotBlank(message = "Product ID is required")
        @Schema(description = "Product identifier", example = "PROD001")
        String productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        @Schema(description = "Product quantity", example = "1", defaultValue = "1")
        Integer quantity,

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        @Schema(description = "Unit price of the product", example = "100.00", defaultValue = "1.00")
        BigDecimal unitPrice
) {}