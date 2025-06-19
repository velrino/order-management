package com.b2b.ordermanagement.application.interfaces;

import com.b2b.ordermanagement.domain.enums.OrderStatus;

import java.time.LocalDateTime;

public interface OrderFilterParams {
    String getPartnerId();
    OrderStatus getStatus();
    LocalDateTime getStartDate();
    LocalDateTime getEndDate();

    default boolean hasPartnerId() {
        return getPartnerId() != null && !getPartnerId().trim().isEmpty();
    }

    default boolean hasStatus() {
        return getStatus() != null;
    }

    default boolean hasDateRange() {
        return getStartDate() != null && getEndDate() != null;
    }

    default boolean hasFilters() {
        return hasPartnerId() || hasStatus() || hasDateRange();
    }
}

