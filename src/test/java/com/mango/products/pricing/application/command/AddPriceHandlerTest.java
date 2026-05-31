package com.mango.products.pricing.application.command;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPriceHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PricingRepository pricingRepository;

    private AddPriceHandler handler;

    private final UUID productId = UUID.randomUUID();
    private final Product product = new Product(ProductId.of(productId), "Zapatillas", "Modelo 2025");

    @BeforeEach
    void setUp() {
        handler = new AddPriceHandler(productRepository, pricingRepository);
    }

    @Test
    void shouldSavePriceWhenNoOverlapExists() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of());

        AddPriceCommand command = new AddPriceCommand(
                productId,
                new BigDecimal("99.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        );

        UUID result = handler.handle(command);

        assertNotNull(result);
        verify(pricingRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowWhenOverlapExists() {
        Price existing = new Price(
                PriceId.generate(),
                ProductId.of(productId),
                new PriceValue(new BigDecimal("99.99")),
                new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30))
        );

        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of(existing));

        AddPriceCommand command = new AddPriceCommand(
                productId,
                new BigDecimal("109.99"),
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2024, 12, 31)
        );

        assertThrows(PriceOverlapException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.empty());

        AddPriceCommand command = new AddPriceCommand(
                productId,
                new BigDecimal("99.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        );

        assertThrows(ProductNotFoundException.class, () -> handler.handle(command));
        verify(pricingRepository, never()).save(any());
    }

    @Test
    void shouldSavePriceWithOpenEndedRange() {
        when(productRepository.findById(ProductId.of(productId))).thenReturn(Optional.of(product));
        when(pricingRepository.findByProductId(ProductId.of(productId))).thenReturn(List.of());

        AddPriceCommand command = new AddPriceCommand(
                productId,
                new BigDecimal("199.99"),
                LocalDate.of(2025, 1, 1),
                null
        );

        UUID result = handler.handle(command);

        assertNotNull(result);
        verify(pricingRepository, times(1)).save(any());
    }
}

