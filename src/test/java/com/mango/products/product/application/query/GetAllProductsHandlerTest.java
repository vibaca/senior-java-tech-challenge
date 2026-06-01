package com.mango.products.product.application.query;

import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllProductsHandlerTest {

    @Mock
    private ProductRepository productRepository;

    private GetAllProductsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetAllProductsHandler(productRepository);
    }

    @Test
    void shouldReturnAllProducts() {
        Product first = new Product(ProductId.of(UUID.randomUUID()), "Camisa", "Algodon");
        Product second = new Product(ProductId.of(UUID.randomUUID()), "Zapatillas", "Running");

        when(productRepository.findAll()).thenReturn(List.of(first, second));

        List<Product> result = handler.handle(new GetAllProductsQuery());

        assertEquals(2, result.size());
        assertEquals("Camisa", result.get(0).name());
        assertEquals("Zapatillas", result.get(1).name());
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> result = handler.handle(new GetAllProductsQuery());

        assertEquals(0, result.size());
    }
}

