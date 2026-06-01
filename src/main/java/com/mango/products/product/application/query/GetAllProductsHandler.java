package com.mango.products.product.application.query;

import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetAllProductsHandler {

    private final ProductRepository productRepository;

    public GetAllProductsHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> handle(GetAllProductsQuery query) {
        return productRepository.findAll();
    }
}

