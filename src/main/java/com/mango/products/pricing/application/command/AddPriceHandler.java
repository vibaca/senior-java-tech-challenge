package com.mango.products.pricing.application.command;

import com.mango.products.pricing.domain.exception.PriceOverlapException;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;

import java.util.List;
import java.util.UUID;

public class AddPriceHandler {

    private final ProductRepository productRepository;
    private final PricingRepository pricingRepository;

    public AddPriceHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        this.productRepository = productRepository;
        this.pricingRepository = pricingRepository;
    }

    public UUID handle(AddPriceCommand command) {
        ProductId productId = ProductId.of(command.productId());

        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        PriceId priceId = PriceId.generate();
        Price newPrice = new Price(
                priceId,
                productId,
                new PriceValue(command.value()),
                new DateRange(command.initDate(), command.endDate())
        );

        List<Price> existing = pricingRepository.findByProductId(productId);
        existing.forEach(existingPrice -> {
            if (existingPrice.overlaps(newPrice)) {
                throw new PriceOverlapException(
                        "Price date range overlaps with an existing price for product " + command.productId()
                );
            }
        });

        pricingRepository.save(newPrice);
        return priceId.value();
    }
}

