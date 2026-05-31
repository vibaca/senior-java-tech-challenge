package com.mango.products.pricing.infrastructure.persistence;

import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.pricing.infrastructure.persistence.entity.PriceEntity;
import com.mango.products.pricing.infrastructure.persistence.jpa.SpringDataPriceJpaRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaPricingRepository implements PricingRepository {

    private final SpringDataPriceJpaRepository repository;

    public JpaPricingRepository(SpringDataPriceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Price price) {
        repository.save(toEntity(price));
    }

    @Override
    public List<Price> findByProductId(ProductId productId) {
        return repository.findByProductIdOrderByInitDateAsc(productId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Price> findEffectivePriceAt(ProductId productId, LocalDate date) {
        return repository.findEffectivePrices(productId.value(), date).stream()
                .findFirst()
                .map(this::toDomain);
    }

    private PriceEntity toEntity(Price price) {
        return new PriceEntity(
                price.id().value(),
                price.productId().value(),
                price.value().value(),
                price.dateRange().initDate(),
                price.dateRange().endDate()
        );
    }

    private Price toDomain(PriceEntity entity) {
        return new Price(
                PriceId.of(entity.getId()),
                ProductId.of(entity.getProductId()),
                new PriceValue(entity.getValue()),
                new DateRange(entity.getInitDate(), entity.getEndDate())
        );
    }
}

