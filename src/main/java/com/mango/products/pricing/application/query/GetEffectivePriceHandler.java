package com.mango.products.pricing.application.query;

import com.mango.products.pricing.domain.exception.PriceNotFoundException;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.product.domain.valueobject.ProductId;

public class GetEffectivePriceHandler {

    private final PricingRepository pricingRepository;

    public GetEffectivePriceHandler(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    public Price handle(GetEffectivePriceQuery query) {
        return pricingRepository
                .findEffectivePriceAt(ProductId.of(query.productId()), query.date())
                .orElseThrow(() -> new PriceNotFoundException(query.productId(), query.date()));
    }
}

