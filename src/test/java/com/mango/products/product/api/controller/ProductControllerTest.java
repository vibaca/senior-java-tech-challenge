package com.mango.products.product.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mango.products.product.api.dto.CreateProductRequest;
import com.mango.products.product.application.command.CreateProductHandler;
import com.mango.products.product.application.query.GetProductHandler;
import com.mango.products.product.domain.model.Product;
import com.mango.products.product.domain.valueobject.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateProductHandler createProductHandler;

    @Autowired
    private GetProductHandler getProductHandler;

    @Test
    void shouldCreateProductAndReturnId() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Zapatillas", "Modelo 2025");

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").exists());
    }

    @Test
    void shouldGetProductByIdAndReturnData() throws Exception {
        com.mango.products.product.application.command.CreateProductCommand cmd =
                new com.mango.products.product.application.command.CreateProductCommand("Zapatillas", "Modelo 2025");
        UUID productId = createProductHandler.handle(cmd);

        mockMvc.perform(get("/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Zapatillas"))
                .andExpect(jsonPath("$.description").value("Modelo 2025"));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/products/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}


