package com.mango.products.pricing.domain.exception;

import java.util.UUID;

public class PriceNotFoundByIdException extends DomainException {

    public PriceNotFoundByIdException(UUID priceId) {
        super("Price not found with id: " + priceId);
    }
}

