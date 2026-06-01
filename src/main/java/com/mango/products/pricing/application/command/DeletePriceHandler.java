package com.mango.products.pricing.application.command;

import com.mango.products.pricing.domain.exception.PriceNotFoundByIdException;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.transaction.annotation.Transactional;

public class DeletePriceHandler {

    private final ProductRepository productRepository;
    private final PricingRepository pricingRepository;

    public DeletePriceHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        this.productRepository = productRepository;
        this.pricingRepository = pricingRepository;
    }

    @Transactional
    public void handle(DeletePriceCommand command) {
        ProductId productId = ProductId.of(command.productId());
        PriceId priceId = PriceId.of(command.priceId());

        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Price existingPrice = pricingRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundByIdException(command.priceId()));

        if (!existingPrice.productId().equals(productId)) {
            throw new PriceNotFoundByIdException(command.priceId());
        }

        pricingRepository.deleteById(priceId);
    }
}

