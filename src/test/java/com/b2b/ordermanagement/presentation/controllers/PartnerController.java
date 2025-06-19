package com.b2b.ordermanagement.presentation.controllers;

import com.b2b.ordermanagement.application.dto.PagedResponse;
import com.b2b.ordermanagement.application.dto.PartnerResponseDTO;
import com.b2b.ordermanagement.application.interfaces.PartnerFilterParams;
import com.b2b.ordermanagement.application.services.PartnerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartnerController.class)
@DisplayName("PartnerController Tests")
class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/v1/partners - Get partners with Pagination and Filters")
    class GetPartnersWithFiltersTests {

        @Test
        @DisplayName("Should return paginated partners with default parameters")
        void getPartner_WithDefaultParams_ShouldReturnPaginated() throws Exception {
            // Arrange
            PartnerResponseDTO partner1 = new PartnerResponseDTO("COMPANY1", "PARTNER1", BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), LocalDateTime.now(), LocalDateTime.now());
            PartnerResponseDTO partner2 = new PartnerResponseDTO("COMPANY2", "PARTNER2", BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), LocalDateTime.now(), LocalDateTime.now());

            Page<PartnerResponseDTO> page = new PageImpl<>(List.of(partner1, partner2));
            PagedResponse<PartnerResponseDTO> pagedResponse = PagedResponse.of(page);

            when(partnerService.getFiltered(any(PartnerFilterParams.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/partners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").isArray())
                    .andExpect(jsonPath("$.records", hasSize(2)))
                    .andExpect(jsonPath("$.records[0].id").value(partner1.id()))
                    .andExpect(jsonPath("$.records[1].id").value(partner2.id()))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.pages").value(1));
        }
    }
}