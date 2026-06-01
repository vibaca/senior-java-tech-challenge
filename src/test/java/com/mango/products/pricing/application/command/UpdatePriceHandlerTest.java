package com.mango.products.pricing.application.command;

import com.mango.products.pricing.domain.exception.PriceNotFoundByIdException;
import com.mango.products.pricing.domain.exception.PriceOverlapException;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePriceHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PricingRepository pricingRepository;

    private UpdatePriceHandler handler;

    private final UUID productId = UUID.randomUUID();
    private final UUID priceId = UUID.randomUUID();
    private final Product product = new Product(ProductId.of(productId), "Zapatillas", "Modelo 2025");

    @BeforeEach
    void setUp() {
        handler = new UpdatePriceHandler(productRepository, pricingRepository);
    }

    @Test
    void shouldUpdatePriceWhenNoOverlapExists() {
        Price existingPrice = new Price(
                PriceId.of(priceId),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("99.99")),
                new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))
        );

        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findById(PriceId.of(priceId))).thenReturn(Optional.of(existingPrice));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of(existingPrice));

        UpdatePriceCommand command = new UpdatePriceCommand(
                productId,
                priceId,
                new BigDecimal("109.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 15)
        );

        assertDoesNotThrow(() -> handler.handle(command));
        verify(pricingRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowWhenUpdatedRangeOverlapsOtherPrice() {
        Price currentPrice = new Price(
                PriceId.of(priceId),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("99.99")),
                new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))
        );
        Price otherPrice = new Price(
                PriceId.generate(),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("199.99")),
                new DateRange(LocalDate.of(2024, 7, 1), null)
        );

        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findById(PriceId.of(priceId))).thenReturn(Optional.of(currentPrice));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of(currentPrice, otherPrice));

        UpdatePriceCommand command = new UpdatePriceCommand(
                productId,
                priceId,
                new BigDecimal("109.99"),
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2024, 7, 15)
        );

        assertThrows(PriceOverlapException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.empty());

        UpdatePriceCommand command = new UpdatePriceCommand(
                productId,
                priceId,
                new BigDecimal("109.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 15)
        );

        assertThrows(ProductNotFoundException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPriceNotFound() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findById(PriceId.of(priceId))).thenReturn(Optional.empty());

        UpdatePriceCommand command = new UpdatePriceCommand(
                productId,
                priceId,
                new BigDecimal("109.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 15)
        );

        assertThrows(PriceNotFoundByIdException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).save(any());
    }
}

