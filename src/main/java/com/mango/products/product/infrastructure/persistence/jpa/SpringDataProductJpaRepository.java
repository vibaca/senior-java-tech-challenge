package com.mango.products.product.infrastructure.persistence.jpa;

import com.mango.products.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
}

