package com.mango.products.product.domain.model;

import com.mango.products.product.domain.valueobject.ProductId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductSuccessfully() {
        Product product = new Product(ProductId.generate(), "Zapatillas", "Modelo 2025");

        assertEquals("Zapatillas", product.name());
        assertEquals("Modelo 2025", product.description());
        assertNotNull(product.id());
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product(ProductId.generate(), "  ", "desc"));
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product(ProductId.generate(), null, "desc"));
    }

    @Test
    void shouldThrowWhenDescriptionIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product(ProductId.generate(), "Zapatillas", ""));
    }

    @Test
    void shouldThrowWhenDescriptionIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product(ProductId.generate(), "Zapatillas", null));
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Product(null, "Zapatillas", "Modelo 2025"));
    }
}


