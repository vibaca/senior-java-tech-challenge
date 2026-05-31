package com.mango.products.pricing.domain.exception;

import java.time.LocalDate;
import java.util.UUID;

public class PriceNotFoundException extends DomainException {

    public PriceNotFoundException(UUID productId, LocalDate date) {
        super("No price found for product " + productId + " on date " + date);
    }
}

