package com.mango.products.pricing.application.query;

import com.mango.products.pricing.domain.exception.PriceNotFoundException;
import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEffectivePriceHandlerTest {

    @Mock
    private PricingRepository pricingRepository;

    private GetEffectivePriceHandler handler;

    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new GetEffectivePriceHandler(pricingRepository);
    }

    @Test
    void shouldReturnEffectivePriceForDate() {
        LocalDate date = LocalDate.of(2024, 4, 15);
        Price price = new Price(
                PriceId.generate(),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("99.99")),
                new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))
        );

        when(pricingRepository.findEffectivePriceAt(ProductId.of(productId), date))
                .thenReturn(Optional.of(price));

        Price result = handler.handle(new GetEffectivePriceQuery(productId, date));

        assertEquals(new BigDecimal("99.99"), result.value().value());
    }

    @Test
    void shouldThrowWhenNoPriceFoundForDate() {
        LocalDate date = LocalDate.of(2024, 4, 15);

        when(pricingRepository.findEffectivePriceAt(ProductId.of(productId), date))
                .thenReturn(Optional.empty());

        assertThrows(PriceNotFoundException.class,
                () -> handler.handle(new GetEffectivePriceQuery(productId, date)));
    }
}

