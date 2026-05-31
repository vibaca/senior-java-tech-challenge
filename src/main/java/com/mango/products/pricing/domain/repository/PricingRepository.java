package com.mango.products.pricing.domain.repository;

import com.mango.products.pricing.domain.model.Price;
import com.mango.products.product.domain.valueobject.ProductId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PricingRepository {
    void save(Price price);
    List<Price> findByProductId(ProductId productId);
    Optional<Price> findEffectivePriceAt(ProductId productId, LocalDate date);
}


