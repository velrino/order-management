package com.b2b.ordermanagement.application.dto;

import com.b2b.ordermanagement.application.interfaces.PartnerFilterParams;

import java.time.LocalDateTime;

public class PartnerFilterDTO implements PartnerFilterParams {
    private String partnerId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public PartnerFilterDTO() {}

    public PartnerFilterDTO(String partnerId, LocalDateTime startDate, LocalDateTime endDate) {
        this.partnerId = partnerId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    @Override
    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }

    @Override
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    @Override
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
