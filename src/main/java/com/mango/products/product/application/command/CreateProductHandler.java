package com.mango.products.product.application.command;

import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class CreateProductHandler {

    private final ProductRepository productRepository;

    public CreateProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public UUID handle(CreateProductCommand command) {
        ProductId id = ProductId.generate();
        Product product = new Product(id, command.name(), command.description());
        productRepository.save(product);
        return id.value();
    }
}

