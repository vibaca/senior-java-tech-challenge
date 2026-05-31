package com.mango.products.pricing.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mango.products.pricing.api.dto.AddPriceRequest;
import com.mango.products.pricing.application.command.AddPriceHandler;
import com.mango.products.pricing.application.command.AddPriceCommand;
import com.mango.products.product.application.command.CreateProductCommand;
import com.mango.products.product.application.command.CreateProductHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateProductHandler createProductHandler;

    @Autowired
    private AddPriceHandler addPriceHandler;

    private UUID productId;

    @BeforeEach
    void setUp() {
        CreateProductCommand cmd = new CreateProductCommand("Zapatillas", "Modelo 2025");
        productId = createProductHandler.handle(cmd);
    }

    @Test
    void shouldAddPriceAndReturnId() throws Exception {
        AddPriceRequest request = new AddPriceRequest(
                new BigDecimal("99.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        );

        mockMvc.perform(post("/products/" + productId + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldGetPriceHistoryForProduct() throws Exception {
        addPriceHandler.handle(new AddPriceCommand(
                productId,
                new BigDecimal("99.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        ));

        mockMvc.perform(get("/products/" + productId + "/prices")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prices").isArray())
                .andExpect(jsonPath("$.prices[0].value").value(99.99));
    }

    @Test
    void shouldGetEffectivePriceByDate() throws Exception {
        addPriceHandler.handle(new AddPriceCommand(
                productId,
                new BigDecimal("99.99"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        ));

        mockMvc.perform(get("/products/" + productId + "/prices?date=2024-04-15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(99.99));
    }

    @Test
    void shouldReturn404WhenNoPriceFoundForDate() throws Exception {
        mockMvc.perform(get("/products/" + productId + "/prices?date=2024-04-15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

