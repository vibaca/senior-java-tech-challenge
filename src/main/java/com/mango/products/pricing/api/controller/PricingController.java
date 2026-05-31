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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/prices")
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
    public ResponseEntity<PriceIdResponse> addPrice(
            @PathVariable UUID productId,
            @RequestBody AddPriceRequest request
    ) {
        AddPriceCommand command = new AddPriceCommand(productId, request.value(), request.initDate(), request.endDate());
        UUID priceId = addPriceHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PriceIdResponse(priceId));
    }

    @GetMapping
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

