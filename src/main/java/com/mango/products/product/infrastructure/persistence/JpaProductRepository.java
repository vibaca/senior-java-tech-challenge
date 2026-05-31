package com.mango.products.product.infrastructure.persistence;

import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.repository.ProductRepository;
import com.mango.products.product.domain.valueobject.ProductId;
import com.mango.products.product.infrastructure.persistence.entity.ProductEntity;
import com.mango.products.product.infrastructure.persistence.jpa.SpringDataProductJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaProductRepository implements ProductRepository {

    private final SpringDataProductJpaRepository repository;

    public JpaProductRepository(SpringDataProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Product product) {
        repository.save(toEntity(product));
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findById(id.value())
                .map(this::toDomain);
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.id().value(),
                product.name(),
                product.description()
        );
    }

    private Product toDomain(ProductEntity entity) {
        return new Product(
                ProductId.of(entity.getId()),
                entity.getName(),
                entity.getDescription()
        );
    }
}

