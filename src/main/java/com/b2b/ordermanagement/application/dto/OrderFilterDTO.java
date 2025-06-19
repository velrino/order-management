package com.b2b.ordermanagement.application.dto;

import com.b2b.ordermanagement.application.interfaces.OrderFilterParams;
import com.b2b.ordermanagement.domain.enums.OrderStatus;

import java.time.LocalDateTime;

public class OrderFilterDTO implements OrderFilterParams {
    private String partnerId;
    private OrderStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public OrderFilterDTO() {}

    public OrderFilterDTO(String partnerId, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        this.partnerId = partnerId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    @Override
    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }

    @Override
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    @Override
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
