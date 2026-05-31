package com.mango.products.product.application.command;

import com.mango.products.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductHandlerTest {

    @Mock
    private ProductRepository productRepository;

    private CreateProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateProductHandler(productRepository);
    }

    @Test
    void shouldReturnGeneratedProductId() {
        CreateProductCommand command = new CreateProductCommand("Zapatillas", "Modelo 2025");

        UUID result = handler.handle(command);

        assertNotNull(result);
    }

    @Test
    void shouldSaveProductWithCorrectData() {
        CreateProductCommand command = new CreateProductCommand("Zapatillas", "Modelo 2025");

        handler.handle(command);

        var captor = ArgumentCaptor.forClass(com.mango.products.product.domain.model.Product.class);
        verify(productRepository, times(1)).save(captor.capture());

        assertEquals("Zapatillas", captor.getValue().name());
        assertEquals("Modelo 2025", captor.getValue().description());
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        CreateProductCommand command = new CreateProductCommand("  ", "desc");

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDescriptionIsNull() {
        CreateProductCommand command = new CreateProductCommand("Zapatillas", null);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(productRepository, never()).save(any());
    }
}

