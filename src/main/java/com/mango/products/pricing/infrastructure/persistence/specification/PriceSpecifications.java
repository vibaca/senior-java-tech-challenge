package com.mango.products.pricing.infrastructure.persistence.specification;

import com.mango.products.pricing.infrastructure.persistence.entity.PriceEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PriceSpecifications {

    private PriceSpecifications() {
        // Utility class
    }

    public static Specification<PriceEntity> forProduct(UUID productId) {
        return (root, query, cb) -> cb.equal(root.get("productId"), productId);
    }

    public static Specification<PriceEntity> valueGreaterOrEqual(BigDecimal minValue) {
        return (root, query, cb) -> minValue == null ? null :
                cb.greaterThanOrEqualTo(root.get("value"), minValue);
    }

    public static Specification<PriceEntity> valueLessOrEqual(BigDecimal maxValue) {
        return (root, query, cb) -> maxValue == null ? null :
                cb.lessThanOrEqualTo(root.get("value"), maxValue);
    }

    public static Specification<PriceEntity> dateRangeIntersects(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate != null && endDate == null) {
                // Filter prices that end on or after the start date
                return cb.or(
                        cb.isNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), startDate)
                );
            }
            if (startDate == null) {
                // Filter prices that start on or before the end date
                return cb.lessThanOrEqualTo(root.get("initDate"), endDate);
            }
            // Both dates provided: find prices that overlap with the range
            return cb.and(
                    cb.lessThanOrEqualTo(root.get("initDate"), endDate),
                    cb.or(
                            cb.isNull(root.get("endDate")),
                            cb.greaterThanOrEqualTo(root.get("endDate"), startDate)
                    )
            );
        };
    }
}

