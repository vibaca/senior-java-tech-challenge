package com.mango.products.pricing.infrastructure.persistence;

import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryPricingRepository implements PricingRepository {

    private final List<Price> store = new ArrayList<>();

    @Override
    public void save(Price price) {
        store.add(price);
    }

    @Override
    public List<Price> findByProductId(ProductId productId) {
        return store.stream()
                .filter(price -> price.productId().equals(productId))
                .toList();
    }

    @Override
    public Optional<Price> findEffectivePriceAt(ProductId productId, LocalDate date) {
        return store.stream()
                .filter(price -> price.productId().equals(productId))
                .filter(price -> price.isEffectiveOn(date))
                .findFirst();
    }
}

