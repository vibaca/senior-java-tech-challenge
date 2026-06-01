package com.mango.products.pricing.application.command;

import com.mango.products.pricing.domain.exception.PriceNotFoundByIdException;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePriceHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PricingRepository pricingRepository;

    private DeletePriceHandler handler;

    private final UUID productId = UUID.randomUUID();
    private final UUID priceId = UUID.randomUUID();
    private final Product product = new Product(ProductId.of(productId), "Zapatillas", "Modelo 2025");

    @BeforeEach
    void setUp() {
        handler = new DeletePriceHandler(productRepository, pricingRepository);
    }

    @Test
    void shouldDeletePriceWhenItExists() {
        Price price = new Price(
                PriceId.of(priceId),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("99.99")),
                new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))
        );

        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findById(PriceId.of(priceId))).thenReturn(Optional.of(price));

        DeletePriceCommand command = new DeletePriceCommand(productId, priceId);

        assertDoesNotThrow(() -> handler.handle(command));
        verify(pricingRepository, times(1)).deleteById(PriceId.of(priceId));
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.empty());

        DeletePriceCommand command = new DeletePriceCommand(productId, priceId);

        assertThrows(ProductNotFoundException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).deleteById(PriceId.of(priceId));
    }

    @Test
    void shouldThrowWhenPriceNotFound() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findById(PriceId.of(priceId))).thenReturn(Optional.empty());

        DeletePriceCommand command = new DeletePriceCommand(productId, priceId);

        assertThrows(PriceNotFoundByIdException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).deleteById(PriceId.of(priceId));
    }
}

