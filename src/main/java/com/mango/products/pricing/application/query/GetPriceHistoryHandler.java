package com.mango.products.pricing.application.query;

import com.mango.products.pricing.api.dto.PriceHistoryPageResponse;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetPriceHistoryHandler {

    private final ProductRepository productRepository;
    private final PricingRepository pricingRepository;

    public GetPriceHistoryHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        this.productRepository = productRepository;
        this.pricingRepository = pricingRepository;
    }

    @Transactional(readOnly = true)
    public List<Price> handle(GetPriceHistoryQuery query) {
        ProductId productId = ProductId.of(query.productId());

        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));

        return pricingRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public PriceHistoryPageResponse handleWithFilters(GetPriceHistoryQuery query) {
        ProductId productId = ProductId.of(query.productId());

        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));

        return pricingRepository.findByProductIdWithFilters(productId, query.filter());
    }
}

