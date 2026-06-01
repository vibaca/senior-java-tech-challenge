package com.mango.products.pricing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceFilterCriteria(
        int page,
        int size,
        String sortBy,
        String sortDirection,
        BigDecimal minValue,
        BigDecimal maxValue,
        LocalDate startDate,
        LocalDate endDate
) {
    public PriceFilterCriteria {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("minValue cannot be greater than maxValue");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
    }

    public static PriceFilterCriteria ofDefaults(int page, int size) {
        return new PriceFilterCriteria(page, size, "initDate", "ASC", null, null, null, null);
    }
}

