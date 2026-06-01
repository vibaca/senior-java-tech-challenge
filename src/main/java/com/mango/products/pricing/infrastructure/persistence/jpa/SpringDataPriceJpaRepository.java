package com.mango.products.pricing.infrastructure.persistence.jpa;

import com.mango.products.pricing.infrastructure.persistence.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpringDataPriceJpaRepository extends JpaRepository<PriceEntity, UUID>, JpaSpecificationExecutor<PriceEntity> {

    List<PriceEntity> findByProductIdOrderByInitDateAsc(UUID productId);

    @Query("""
            select p
            from PriceEntity p
            where p.productId = :productId
              and p.initDate <= :date
              and (p.endDate is null or p.endDate >= :date)
            order by p.initDate desc
            """)
    List<PriceEntity> findEffectivePrices(@Param("productId") UUID productId, @Param("date") LocalDate date);
}

