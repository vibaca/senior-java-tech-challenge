package com.mango.products.product.domain.model;

import com.mango.products.product.domain.valueobject.ProductId;

import java.util.Objects;

public class Product {

    private final ProductId id;
    private final String name;
    private final String description;

    public Product(ProductId id, String name, String description) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = requireNonBlank(name, "name");
        this.description = requireNonBlank(description, "description");
    }

    public ProductId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    private String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }
}


