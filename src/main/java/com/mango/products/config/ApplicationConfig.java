package com.mango.products.config;

import com.mango.products.pricing.application.command.AddPriceHandler;
import com.mango.products.pricing.application.command.DeletePriceHandler;
import com.mango.products.pricing.application.command.UpdatePriceHandler;
import com.mango.products.pricing.application.query.GetEffectivePriceHandler;
import com.mango.products.pricing.application.query.GetPriceHistoryHandler;
import com.mango.products.pricing.domain.repository.PricingRepository;
import com.mango.products.product.application.command.CreateProductHandler;
import com.mango.products.product.application.query.GetAllProductsHandler;
import com.mango.products.product.application.query.GetProductHandler;
import com.mango.products.product.domain.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public CreateProductHandler createProductHandler(ProductRepository productRepository) {
        return new CreateProductHandler(productRepository);
    }

    @Bean
    public GetProductHandler getProductHandler(ProductRepository productRepository) {
        return new GetProductHandler(productRepository);
    }

    @Bean
    public GetAllProductsHandler getAllProductsHandler(ProductRepository productRepository) {
        return new GetAllProductsHandler(productRepository);
    }

    @Bean
    public AddPriceHandler addPriceHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        return new AddPriceHandler(productRepository, pricingRepository);
    }

    @Bean
    public UpdatePriceHandler updatePriceHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        return new UpdatePriceHandler(productRepository, pricingRepository);
    }

    @Bean
    public DeletePriceHandler deletePriceHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        return new DeletePriceHandler(productRepository, pricingRepository);
    }

    @Bean
    public GetEffectivePriceHandler getEffectivePriceHandler(PricingRepository pricingRepository) {
        return new GetEffectivePriceHandler(pricingRepository);
    }

    @Bean
    public GetPriceHistoryHandler getPriceHistoryHandler(ProductRepository productRepository, PricingRepository pricingRepository) {
        return new GetPriceHistoryHandler(productRepository, pricingRepository);
    }
}

