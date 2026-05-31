package com.mango.products.pricing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PriceDTO(
        UUID id,
        BigDecimal value,
        LocalDate initDate,
        LocalDate endDate
) {
}

