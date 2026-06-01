package com.mango.products.pricing.infrastructure.persistence;

import com.mango.products.pricing.api.dto.PriceFilterCriteria;
import com.mango.products.pricing.api.dto.PriceDTO;
import com.mango.products.pricing.api.dto.PriceHistoryPageResponse;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.pricing.infrastructure.persistence.entity.PriceEntity;
import com.mango.products.pricing.infrastructure.persistence.jpa.SpringDataPriceJpaRepository;
import com.mango.products.pricing.infrastructure.persistence.specification.PriceSpecifications;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public Optional<Price> findById(PriceId priceId) {
        return repository.findById(priceId.value()).map(this::toDomain);
    }

    @Override
    public void deleteById(PriceId priceId) {
        repository.deleteById(priceId.value());
    }

    @Override
    public Optional<Price> findEffectivePriceAt(ProductId productId, LocalDate date) {
        return repository.findEffectivePrices(productId.value(), date).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public PriceHistoryPageResponse findByProductIdWithFilters(ProductId productId, PriceFilterCriteria criteria) {
        Sort sort = Sort.by(
                new Sort.Order(
                        "DESC".equalsIgnoreCase(criteria.sortDirection())
                                ? Sort.Direction.DESC : Sort.Direction.ASC,
                        criteria.sortBy()
                )
        );

        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        Specification<PriceEntity> spec = Specification.where(PriceSpecifications.forProduct(productId.value()))
                .and(PriceSpecifications.valueGreaterOrEqual(criteria.minValue()))
                .and(PriceSpecifications.valueLessOrEqual(criteria.maxValue()))
                .and(PriceSpecifications.dateRangeIntersects(criteria.startDate(), criteria.endDate()));

        Page<PriceEntity> page = repository.findAll(spec, pageable);

        List<PriceDTO> pricesDTOs = page.getContent().stream()
                .map(entity -> new PriceDTO(
                        entity.getId(),
                        entity.getValue(),
                        entity.getInitDate(),
                        entity.getEndDate()
                ))
                .toList();

        return new PriceHistoryPageResponse(
                pricesDTOs,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
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

