package com.b2b.ordermanagement.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderDTO(
        @NotBlank()
        @Schema(example = "PARTNER001")
        String partnerId,

        @NotNull()
        @NotEmpty()
        @Valid
        List<OrderItemDTO> items
) {}