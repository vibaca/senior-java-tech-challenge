package com.mango.products.product.api.controller;

import com.mango.products.product.api.dto.CreateProductRequest;
import com.mango.products.product.api.dto.CreateProductResponse;
import com.mango.products.product.application.command.CreateProductCommand;
import com.mango.products.product.application.command.CreateProductHandler;
import com.mango.products.product.application.query.GetProductHandler;
import com.mango.products.product.application.query.GetProductQuery;
import com.mango.products.product.domain.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductHandler createProductHandler;
    private final GetProductHandler getProductHandler;

    public ProductController(CreateProductHandler createProductHandler, GetProductHandler getProductHandler) {
        this.createProductHandler = createProductHandler;
        this.getProductHandler = getProductHandler;
    }

    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(request.name(), request.description());
        UUID productId = createProductHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateProductResponse(productId));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable UUID productId) {
        Product product = getProductHandler.handle(new GetProductQuery(productId));
        ProductDTO dto = new ProductDTO(
                product.id().value(),
                product.name(),
                product.description()
        );
        return ResponseEntity.ok(dto);
    }

    public record ProductDTO(UUID id, String name, String description) {
    }
}

