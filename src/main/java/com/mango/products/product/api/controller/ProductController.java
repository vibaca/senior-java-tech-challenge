package com.mango.products.product.api.controller;

import com.mango.products.product.api.dto.CreateProductRequest;
import com.mango.products.product.api.dto.CreateProductResponse;
import com.mango.products.product.application.command.CreateProductCommand;
import com.mango.products.product.application.command.CreateProductHandler;
import com.mango.products.product.application.query.GetProductHandler;
import com.mango.products.product.application.query.GetProductQuery;
import com.mango.products.product.domain.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final CreateProductHandler createProductHandler;
    private final GetProductHandler getProductHandler;

    public ProductController(CreateProductHandler createProductHandler, GetProductHandler getProductHandler) {
        this.createProductHandler = createProductHandler;
        this.getProductHandler = getProductHandler;
    }

    @PostMapping
    @Operation(summary = "Create a product", description = "Creates a new product and returns its identifier")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(request.name(), request.description());
        UUID productId = createProductHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateProductResponse(productId));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by id", description = "Returns a product by identifier")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
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

