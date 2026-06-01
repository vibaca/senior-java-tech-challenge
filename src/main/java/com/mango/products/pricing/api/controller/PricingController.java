package com.mango.products.pricing.api.controller;

import com.mango.products.pricing.api.dto.AddPriceRequest;
import com.mango.products.pricing.api.dto.PriceDTO;
import com.mango.products.pricing.api.dto.PriceFilterCriteria;
import com.mango.products.pricing.api.dto.PriceHistoryPageResponse;
import com.mango.products.pricing.api.dto.UpdatePriceRequest;
import com.mango.products.pricing.application.command.AddPriceCommand;
import com.mango.products.pricing.application.command.AddPriceHandler;
import com.mango.products.pricing.application.command.DeletePriceCommand;
import com.mango.products.pricing.application.command.DeletePriceHandler;
import com.mango.products.pricing.application.command.UpdatePriceCommand;
import com.mango.products.pricing.application.command.UpdatePriceHandler;
import com.mango.products.pricing.application.query.GetEffectivePriceHandler;
import com.mango.products.pricing.application.query.GetEffectivePriceQuery;
import com.mango.products.pricing.application.query.GetPriceHistoryHandler;
import com.mango.products.pricing.application.query.GetPriceHistoryQuery;
import com.mango.products.pricing.domain.model.Price;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/prices")
@Tag(name = "Pricing", description = "Historical pricing and effective price endpoints")
public class PricingController {

    private final AddPriceHandler addPriceHandler;
    private final UpdatePriceHandler updatePriceHandler;
    private final DeletePriceHandler deletePriceHandler;
    private final GetEffectivePriceHandler getEffectivePriceHandler;
    private final GetPriceHistoryHandler getPriceHistoryHandler;

    public PricingController(
            AddPriceHandler addPriceHandler,
            UpdatePriceHandler updatePriceHandler,
            DeletePriceHandler deletePriceHandler,
            GetEffectivePriceHandler getEffectivePriceHandler,
            GetPriceHistoryHandler getPriceHistoryHandler
    ) {
        this.addPriceHandler = addPriceHandler;
        this.updatePriceHandler = updatePriceHandler;
        this.deletePriceHandler = deletePriceHandler;
        this.getEffectivePriceHandler = getEffectivePriceHandler;
        this.getPriceHistoryHandler = getPriceHistoryHandler;
    }

    @PostMapping
    @Operation(summary = "Add product price", description = "Adds a new historical price for a product")
    @SecurityRequirement(name = "bearerAuth")
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

    @PutMapping("/{priceId}")
    @Operation(summary = "Update product price", description = "Updates an existing historical price by id")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid price request",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Price or product not found",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Price range overlap",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<PriceIdResponse> updatePrice(
            @PathVariable UUID productId,
            @PathVariable UUID priceId,
            @RequestBody UpdatePriceRequest request
    ) {
        UpdatePriceCommand command = new UpdatePriceCommand(
                productId,
                priceId,
                request.value(),
                request.initDate(),
                request.endDate()
        );
        UUID updatedPriceId = updatePriceHandler.handle(command);
        return ResponseEntity.ok(new PriceIdResponse(updatedPriceId));
    }

    @DeleteMapping("/{priceId}")
    @Operation(summary = "Delete product price", description = "Deletes a historical price by id")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Price deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Price or product not found",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deletePrice(
            @PathVariable UUID productId,
            @PathVariable UUID priceId
    ) {
        deletePriceHandler.handle(new DeletePriceCommand(productId, priceId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get product prices", description = "Returns price history or effective price when date is provided")
    @SecurityRequirement(name = "bearerAuth")
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
            @Parameter(name = "size", description = "Records per page (1-100, default 10)", example = "10"),
            @Parameter(name = "sort", description = "Sort field: initDate, endDate, or value", example = "initDate"),
            @Parameter(name = "direction", description = "Sort direction: ASC or DESC", example = "ASC"),
            @Parameter(name = "minValue", description = "Minimum price filter", example = "50.00"),
            @Parameter(name = "maxValue", description = "Maximum price filter", example = "150.00"),
            @Parameter(name = "startDate", description = "Filter prices effective from this date (format: YYYY-MM-DD)", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "Filter prices effective until this date (format: YYYY-MM-DD)", example = "2026-12-31"),
            @Parameter(name = "date", description = "Get effective price for specific date (format: YYYY-MM-DD)", example = "2026-06-02")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price data returned"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Price or product not found",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter criteria",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<?> getPrices(
            @PathVariable UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "initDate") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Get effective price for a specific date
        if (date != null) {
            Price price = getEffectivePriceHandler.handle(new GetEffectivePriceQuery(productId, date));
            return ResponseEntity.ok(new EffectivePriceResponse(price.value().value()));
        }

        // Get price history with optional filters and pagination
        PriceFilterCriteria criteria = new PriceFilterCriteria(page, size, sort, direction, minValue, maxValue, startDate, endDate);
        GetPriceHistoryQuery query = new GetPriceHistoryQuery(productId, criteria);
        PriceHistoryPageResponse response = getPriceHistoryHandler.handleWithFilters(query);
        return ResponseEntity.ok(response);
    }

    public record PriceIdResponse(UUID id) {
    }

    public record EffectivePriceResponse(java.math.BigDecimal value) {
    }
}




