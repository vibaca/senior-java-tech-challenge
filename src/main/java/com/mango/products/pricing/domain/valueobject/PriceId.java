package com.mango.products.pricing.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record PriceId(UUID value) {

    public PriceId {
        Objects.requireNonNull(value, "PriceId cannot be null");
    }

    public static PriceId generate() {
        return new PriceId(UUID.randomUUID());
    }

    public static PriceId of(UUID value) {
        return new PriceId(value);
    }
}


