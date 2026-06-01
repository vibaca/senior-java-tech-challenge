package com.mango.products.pricing.application.command;

import java.util.UUID;

public record DeletePriceCommand(UUID productId, UUID priceId) {
}

