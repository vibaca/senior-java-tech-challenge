package com.mango.products.pricing.application.command;

import com.mango.products.pricing.domain.exception.PriceNotFoundByIdException;
import com.mango.products.pricing.domain.exception.PriceOverlapException;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class UpdatePriceHandler {

    private final ProductRepository productRepository;
    private final PricingRepository pricingRepository;

    public UpdatePriceHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        this.productRepository = productRepository;
        this.pricingRepository = pricingRepository;
    }

    @Transactional
    public UUID handle(UpdatePriceCommand command) {
        ProductId productId = ProductId.of(command.productId());
        PriceId priceId = PriceId.of(command.priceId());

        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Price existingPrice = pricingRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundByIdException(command.priceId()));

        if (!existingPrice.productId().equals(productId)) {
            throw new PriceNotFoundByIdException(command.priceId());
        }

        Price updatedPrice = new Price(
                priceId,
                productId,
                new PriceValue(command.value()),
                new DateRange(command.initDate(), command.endDate())
        );

        List<Price> allPrices = pricingRepository.findByProductId(productId);
        allPrices.stream()
                .filter(price -> !price.id().equals(priceId))
                .filter(price -> price.overlaps(updatedPrice))
                .findAny()
                .ifPresent(price -> {
                    throw new PriceOverlapException(
                            "Price date range overlaps with an existing price for product " + command.productId()
                    );
                });

        pricingRepository.save(updatedPrice);
        return priceId.value();
    }
}

