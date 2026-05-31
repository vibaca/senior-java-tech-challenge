package com.mango.products.pricing.application.query;

import com.mango.products.pricing.domain.model.Price;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.pricing.domain.valueobject.DateRange;
import com.mango.products.pricing.domain.valueobject.PriceId;
import com.mango.products.pricing.domain.valueobject.PriceValue;
import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPriceHistoryHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PricingRepository pricingRepository;

    private GetPriceHistoryHandler handler;

    private final UUID productId = UUID.randomUUID();
    private final Product product = new Product(ProductId.of(productId), "Zapatillas", "Modelo 2025");

    @BeforeEach
    void setUp() {
        handler = new GetPriceHistoryHandler(productRepository, pricingRepository);
    }

    @Test
    void shouldReturnPriceHistoryForProduct() {
        Price price1 = new Price(
                PriceId.generate(),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("99.99")),
                new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))
        );
        Price price2 = new Price(
                PriceId.generate(),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("129.99")),
                new DateRange(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31))
        );

        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of(price1, price2));

        List<Price> result = handler.handle(new GetPriceHistoryQuery(productId));

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoPricesExist() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of());

        List<Price> result = handler.handle(new GetPriceHistoryQuery(productId));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> handler.handle(new GetPriceHistoryQuery(productId)));

        verify(pricingRepository, never()).findByProductId(any());
    }
}

