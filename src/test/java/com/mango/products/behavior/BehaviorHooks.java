package com.mango.products.behavior;

import com.mango.products.pricing.infrastructure.persistence.jpa.SpringDataPriceJpaRepository;
import com.mango.products.product.infrastructure.persistence.jpa.SpringDataProductJpaRepository;
import io.cucumber.java.Before;

public class BehaviorHooks {

    private final SpringDataPriceJpaRepository priceRepository;
    private final SpringDataProductJpaRepository productRepository;
    private final BehaviorTestContext context;

    public BehaviorHooks(
            SpringDataPriceJpaRepository priceRepository,
            SpringDataProductJpaRepository productRepository,
            BehaviorTestContext context
    ) {
        this.priceRepository = priceRepository;
        this.productRepository = productRepository;
        this.context = context;
    }

    @Before
    public void cleanDatabase() {
        priceRepository.deleteAll();
        productRepository.deleteAll();
        context.clear();
    }
}

