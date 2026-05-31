package com.mango.products.pricing.domain.valueobject;

import com.mango.products.pricing.domain.exception.DomainException;

import java.math.BigDecimal;
import java.util.Objects;

public record PriceValue(BigDecimal value) {

    public PriceValue {
        Objects.requireNonNull(value, "value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("price value must be greater than zero");
        }
    }
}


