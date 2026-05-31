package com.mango.products.pricing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddPriceRequest(
        BigDecimal value,
        LocalDate initDate,
        LocalDate endDate
) {
}

