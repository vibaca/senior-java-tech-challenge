package com.mango.products.pricing.api.controller;

import com.mango.products.pricing.api.dto.AddPriceRequest;
import com.mango.products.pricing.api.dto.PriceDTO;
import com.mango.products.pricing.application.command.AddPriceCommand;
import com.mango.products.pricing.application.command.AddPriceHandler;
import com.mango.products.pricing.application.query.GetEffectivePriceHandler;
import com.mango.products.pricing.application.query.GetEffectivePriceQuery;
import com.mango.products.pricing.application.query.GetPriceHistoryHandler;
import com.mango.products.pricing.application.query.GetPriceHistoryQuery;
import com.mango.products.pricing.domain.model.Price;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/prices")
@Tag(name = "Pricing", description = "Historical pricing and effective price endpoints")
public class PricingController {

    private final AddPriceHandler addPriceHandler;
    private final GetEffectivePriceHandler getEffectivePriceHandler;
    private final GetPriceHistoryHandler getPriceHistoryHandler;

    public PricingController(
            AddPriceHandler addPriceHandler,
            GetEffectivePriceHandler getEffectivePriceHandler,
            GetPriceHistoryHandler getPriceHistoryHandler
    ) {
        this.addPriceHandler = addPriceHandler;
        this.getEffectivePriceHandler = getEffectivePriceHandler;
        this.getPriceHistoryHandler = getPriceHistoryHandler;
    }

    @PostMapping
    @Operation(summary = "Add product price", description = "Adds a new historical price for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Price created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid price request",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Price range overlap",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<PriceIdResponse> addPrice(
            @PathVariable UUID productId,
            @RequestBody AddPriceRequest request
    ) {
        AddPriceCommand command = new AddPriceCommand(productId, request.value(), request.initDate(), request.endDate());
        UUID priceId = addPriceHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PriceIdResponse(priceId));
    }

    @GetMapping
    @Operation(summary = "Get product prices", description = "Returns price history or effective price when date is provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price data returned"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Price or product not found",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<?> getPrices(
            @PathVariable UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date != null) {
            Price price = getEffectivePriceHandler.handle(new GetEffectivePriceQuery(productId, date));
            return ResponseEntity.ok(new EffectivePriceResponse(price.value().value()));
        } else {
            List<Price> prices = getPriceHistoryHandler.handle(new GetPriceHistoryQuery(productId));
            List<PriceDTO> pricesDTOs = prices.stream()
                    .map(p -> new PriceDTO(
                            p.id().value(),
                            p.value().value(),
                            p.dateRange().initDate(),
                            p.dateRange().endDate()
                    ))
                    .toList();
            return ResponseEntity.ok(new PriceHistoryResponse(pricesDTOs));
        }
    }

    public record PriceIdResponse(UUID id) {
    }

    public record EffectivePriceResponse(java.math.BigDecimal value) {
    }

    public record PriceHistoryResponse(List<PriceDTO> prices) {
    }
}

