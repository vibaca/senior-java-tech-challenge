package com.mango.products.pricing.domain.model;

import com.mango.products.pricing.domain.exception.DomainException;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.product.domain.valueobject.ProductId;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PriceTest {

    private static final ProductId PRODUCT_ID = ProductId.generate();

    private Price buildPrice(LocalDate init, LocalDate end, BigDecimal amount) {
        return new Price(
                PriceId.generate(),
                PRODUCT_ID,
                new PriceValue(amount),
                new DateRange(init, end)
        );
    }

    @Test
    void shouldBeEffectiveOnDateInsideRange() {
        Price price = buildPrice(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), new BigDecimal("99.99"));
        assertTrue(price.isEffectiveOn(LocalDate.of(2024, 4, 15)));
    }

    @Test
    void shouldNotBeEffectiveOutsideRange() {
        Price price = buildPrice(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), new BigDecimal("99.99"));
        assertFalse(price.isEffectiveOn(LocalDate.of(2024, 8, 1)));
    }

    @Test
    void shouldBeEffectiveOnOpenEndedRange() {
        Price price = buildPrice(LocalDate.of(2025, 1, 1), null, new BigDecimal("199.99"));
        assertTrue(price.isEffectiveOn(LocalDate.of(2030, 6, 15)));
    }

    @Test
    void shouldDetectOverlapWithAnotherPrice() {
        Price a = buildPrice(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), new BigDecimal("99.99"));
        Price b = buildPrice(LocalDate.of(2024, 6, 15), LocalDate.of(2024, 12, 31), new BigDecimal("129.99"));
        assertTrue(a.overlaps(b));
    }

    @Test
    void shouldNotDetectOverlapForContiguousPrices() {
        Price a = buildPrice(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), new BigDecimal("99.99"));
        Price b = buildPrice(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), new BigDecimal("129.99"));
        assertFalse(a.overlaps(b));
    }

    @Test
    void shouldThrowWhenPriceValueIsZero() {
        assertThrows(DomainException.class, () -> new PriceValue(BigDecimal.ZERO));
    }

    @Test
    void shouldThrowWhenPriceValueIsNegative() {
        assertThrows(DomainException.class, () -> new PriceValue(new BigDecimal("-1.00")));
    }

    @Test
    void shouldThrowWhenPriceValueIsNull() {
        assertThrows(NullPointerException.class, () -> new PriceValue(null));
    }

    @Test
    void shouldThrowWhenPriceIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Price(null, PRODUCT_ID, new PriceValue(new BigDecimal("99.99")),
                        new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))));
    }
}


