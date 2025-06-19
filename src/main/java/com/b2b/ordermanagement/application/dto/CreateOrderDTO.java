package com.b2b.ordermanagement.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderDTO(
        @NotBlank()
        String partnerId,

        @NotNull()
        @NotEmpty()
        @Valid
        List<OrderItemDTO> items
) {}