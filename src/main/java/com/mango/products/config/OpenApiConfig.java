package com.mango.products.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Products Pricing API")
                        .description("REST API for product and historical pricing management")
                        .version("v1")
                        .contact(new Contact()
                                .name("Mango Engineering")
                                .email("engineering@mango.com"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://mango.com")));
    }
}

