package com.mango.products.pricing.domain.model;

import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.product.domain.valueobject.ProductId;

import java.time.LocalDate;
import java.util.Objects;

public final class Price {

    private final PriceId id;
    private final ProductId productId;
    private final PriceValue value;
    private final DateRange dateRange;

    public Price(PriceId id, ProductId productId, PriceValue value, DateRange dateRange) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.productId = Objects.requireNonNull(productId, "productId cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.dateRange = Objects.requireNonNull(dateRange, "dateRange cannot be null");
    }

    public PriceId id() {
        return id;
    }

    public ProductId productId() {
        return productId;
    }

    public PriceValue value() {
        return value;
    }

    public DateRange dateRange() {
        return dateRange;
    }

    public boolean isEffectiveOn(LocalDate date) {
        return dateRange.contains(date);
    }

    public boolean overlaps(Price other) {
        return this.dateRange.overlaps(other.dateRange);
    }
}


