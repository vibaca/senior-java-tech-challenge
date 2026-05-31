package com.mango.products.product.application.query;

import com.mango.products.product.domain.exception.ProductNotFoundException;
import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import org.springframework.transaction.annotation.Transactional;

public class GetProductHandler {

    private final ProductRepository productRepository;

    public GetProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Product handle(GetProductQuery query) {
        return productRepository.findById(ProductId.of(query.productId()))
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));
    }
}

