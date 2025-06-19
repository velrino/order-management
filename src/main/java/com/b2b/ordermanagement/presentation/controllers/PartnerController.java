package com.b2b.ordermanagement.presentation.controllers;

import com.b2b.ordermanagement.application.dto.OrderFilterDTO;
import com.b2b.ordermanagement.application.dto.PagedResponse;
import com.b2b.ordermanagement.application.dto.PartnerFilterDTO;
import com.b2b.ordermanagement.application.dto.PartnerResponseDTO;
import com.b2b.ordermanagement.application.interfaces.PartnerFilterParams;
import com.b2b.ordermanagement.application.services.PartnerService;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/partners")
@Tag(name = "Partners", description = "Parter management operations")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    @Operation(summary = "Get orders with filters", description = "Retrieves orders filtered by various criteria")
    public ResponseEntity<PagedResponse<PartnerResponseDTO>> getOrders(
            @Parameter(description = "Partner ID") @RequestParam(required = false) String partnerId,
            @Parameter(description = "Start date (ISO format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PartnerFilterParams filters = new PartnerFilterDTO(partnerId, startDate, endDate);
        Page<PartnerResponseDTO> orders = partnerService.getFilteredOrders(filters, pageable);

        return ResponseEntity.ok(PagedResponse.of(orders));
    }
}