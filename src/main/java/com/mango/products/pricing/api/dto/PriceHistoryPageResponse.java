package com.mango.products.pricing.api.dto;

import java.util.List;

public record PriceHistoryPageResponse(
        List<PriceDTO> prices,
        int page,
        int size,
        long total,
        int totalPages
) {
}

