package com.mango.products.product.domain.repository;

import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.valueobject.ProductId;

import java.util.Optional;

public interface ProductRepository {
    void save(Product product);
    Optional<Product> findById(ProductId id);
}


