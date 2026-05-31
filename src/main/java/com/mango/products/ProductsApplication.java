package com.mango.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
	"com.mango.products.product.infrastructure.persistence.entity",
	"com.mango.products.pricing.infrastructure.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
	"com.mango.products.product.infrastructure.persistence.jpa",
	"com.mango.products.pricing.infrastructure.persistence.jpa"
})
public class ProductsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsApplication.class, args);
	}
}