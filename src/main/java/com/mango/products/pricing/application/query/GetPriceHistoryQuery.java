package com.mango.products.pricing.application.query;

import com.mango.products.pricing.api.dto.PriceFilterCriteria;

import java.util.UUID;

public record GetPriceHistoryQuery(UUID productId, PriceFilterCriteria filter) {
    public GetPriceHistoryQuery(UUID productId) {
        this(productId, PriceFilterCriteria.ofDefaults(0, 10));
    }
}

