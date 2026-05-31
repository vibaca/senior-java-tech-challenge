package com.mango.products.pricing.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AddPriceCommand(
        UUID productId,
        BigDecimal value,
        LocalDate initDate,
        LocalDate endDate
) {
}

