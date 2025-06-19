package com.b2b.ordermanagement.application.interfaces;

import java.time.LocalDateTime;

public interface PartnerFilterParams {
    String getPartnerId();
    LocalDateTime getStartDate();
    LocalDateTime getEndDate();

    default boolean hasPartnerId() {
        return getPartnerId() != null && !getPartnerId().trim().isEmpty();
    }

    default boolean hasDateRange() {
        return getStartDate() != null && getEndDate() != null;
    }

    default boolean hasFilters() {
        return hasPartnerId() || hasDateRange();
    }
}

