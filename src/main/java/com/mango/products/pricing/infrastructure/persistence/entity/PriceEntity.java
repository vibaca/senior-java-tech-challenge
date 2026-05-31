package com.mango.products.pricing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "prices",
        indexes = {
                @Index(name = "idx_prices_product_id", columnList = "product_id"),
                @Index(name = "idx_prices_product_init_date", columnList = "product_id, init_date"),
                @Index(name = "idx_prices_product_date_range", columnList = "product_id, init_date, end_date")
        }
)
public class PriceEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "price_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @Column(name = "init_date", nullable = false)
    private LocalDate initDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    protected PriceEntity() {
    }

    public PriceEntity(UUID id, UUID productId, BigDecimal value, LocalDate initDate, LocalDate endDate) {
        this.id = id;
        this.productId = productId;
        this.value = value;
        this.initDate = initDate;
        this.endDate = endDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public LocalDate getInitDate() {
        return initDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}

