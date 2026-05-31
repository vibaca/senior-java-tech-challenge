package com.mango.products.pricing.application.query;

import java.time.LocalDate;
import java.util.UUID;

public record GetEffectivePriceQuery(UUID productId, LocalDate date) {
}

