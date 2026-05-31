package com.mango.products.product.application.query;

import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductHandlerTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetProductHandler(productRepository);
    }

    @Test
    void shouldReturnProductWhenFound() {
        UUID id = UUID.randomUUID();
        Product product = new Product(ProductId.of(id), "Zapatillas", "Modelo 2025");

        when(productRepository.findById(ProductId.of(id))).thenReturn(Optional.of(product));

        Product result = handler.handle(new GetProductQuery(id));

        assertEquals("Zapatillas", result.name());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(ProductId.of(id))).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> handler.handle(new GetProductQuery(id)));
    }
}

