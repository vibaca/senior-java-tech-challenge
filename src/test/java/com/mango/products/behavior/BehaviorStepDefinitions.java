package com.mango.products.behavior;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mango.products.config.security.JwtService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class BehaviorStepDefinitions {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final BehaviorTestContext context;
    private final String authToken;

    public BehaviorStepDefinitions(MockMvc mockMvc, ObjectMapper objectMapper, BehaviorTestContext context, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.authToken = jwtService.generateToken("testuser");
    }

    @Given("a product named {string} with description {string} exists")
    public void aProductExists(String name, String description) throws Exception {
        MvcResult result = createProduct(name, description);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        context.setCurrentProductId(UUID.fromString(readBody(result).get("productId").asText()));
    }

    @Given("a missing product id")
    public void aMissingProductId() {
        context.setMissingProductId(UUID.randomUUID());
    }

    @Given("a missing price id")
    public void aMissingPriceId() {
        context.setMissingPriceId(UUID.randomUUID());
    }

    @Given("the current product has a price of {string} from {string} to {string}")
    public void theCurrentProductHasAPrice(String value, String initDate, String endDate) throws Exception {
        MvcResult result = addPrice(
                context.getCurrentProductId(),
                value,
                LocalDate.parse(initDate),
                parseNullableDate(endDate)
        );
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        context.setCurrentPriceId(UUID.fromString(readBody(result).get("id").asText()));
    }

    @Given("the current product has an open-ended price of {string} starting on {string}")
    public void theCurrentProductHasAnOpenEndedPrice(String value, String initDate) throws Exception {
        MvcResult result = addPrice(context.getCurrentProductId(), value, LocalDate.parse(initDate), null);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        context.setCurrentPriceId(UUID.fromString(readBody(result).get("id").asText()));
    }

    @When("I create a product named {string} with description {string}")
    public void iCreateAProduct(String name, String description) throws Exception {
        context.setLastResult(createProduct(name, description));
    }

    @When("I retrieve the current product")
    public void iRetrieveTheCurrentProduct() throws Exception {
        context.setLastResult(mockMvc.perform(get("/products/{productId}", context.getCurrentProductId())
                .header("Authorization", "Bearer " + authToken))
                .andReturn());
    }

    @When("I retrieve the missing product")
    public void iRetrieveTheMissingProduct() throws Exception {
        context.setLastResult(mockMvc.perform(get("/products/{productId}", context.getMissingProductId())
                .header("Authorization", "Bearer " + authToken))
                .andReturn());
    }

    @When("I add a price of {string} from {string} to {string} to the current product")
    public void iAddAPriceToTheCurrentProduct(String value, String initDate, String endDate) throws Exception {
        context.setLastResult(addPrice(
                context.getCurrentProductId(),
                value,
                LocalDate.parse(initDate),
                parseNullableDate(endDate)
        ));
    }

    @When("I add an open-ended price of {string} starting on {string} to the current product")
    public void iAddAnOpenEndedPriceToTheCurrentProduct(String value, String initDate) throws Exception {
        context.setLastResult(addPrice(context.getCurrentProductId(), value, LocalDate.parse(initDate), null));
    }

    @When("I update the current price to {string} from {string} to {string}")
    public void iUpdateTheCurrentPrice(String value, String initDate, String endDate) throws Exception {
        context.setLastResult(updatePrice(
                context.getCurrentProductId(),
                context.getCurrentPriceId(),
                value,
                LocalDate.parse(initDate),
                parseNullableDate(endDate)
        ));
    }

    @When("I update the missing price to {string} from {string} to {string}")
    public void iUpdateTheMissingPrice(String value, String initDate, String endDate) throws Exception {
        context.setLastResult(updatePrice(
                context.getCurrentProductId(),
                context.getMissingPriceId(),
                value,
                LocalDate.parse(initDate),
                parseNullableDate(endDate)
        ));
    }

    @When("I delete the current price")
    public void iDeleteTheCurrentPrice() throws Exception {
        context.setLastResult(deletePrice(context.getCurrentProductId(), context.getCurrentPriceId()));
    }

    @When("I delete the missing price")
    public void iDeleteTheMissingPrice() throws Exception {
        context.setLastResult(deletePrice(context.getCurrentProductId(), context.getMissingPriceId()));
    }

    @When("I add a price of {string} from {string} to {string} to the missing product")
    public void iAddAPriceToTheMissingProduct(String value, String initDate, String endDate) throws Exception {
        context.setLastResult(addPrice(
                context.getMissingProductId(),
                value,
                LocalDate.parse(initDate),
                parseNullableDate(endDate)
        ));
    }

    @When("I request the effective price for the current product on {string}")
    public void iRequestTheEffectivePriceForTheCurrentProduct(String date) throws Exception {
        context.setLastResult(mockMvc.perform(get("/products/{productId}/prices", context.getCurrentProductId())
                        .header("Authorization", "Bearer " + authToken)
                        .param("date", date))
                .andReturn());
    }

    @When("I request the effective price for the missing product on {string}")
    public void iRequestTheEffectivePriceForTheMissingProduct(String date) throws Exception {
        context.setLastResult(mockMvc.perform(get("/products/{productId}/prices", context.getMissingProductId())
                        .header("Authorization", "Bearer " + authToken)
                        .param("date", date))
                .andReturn());
    }

    @When("I request the price history for the current product")
    public void iRequestThePriceHistoryForTheCurrentProduct() throws Exception {
        context.setLastResult(mockMvc.perform(get("/products/{productId}/prices", context.getCurrentProductId())
                .header("Authorization", "Bearer " + authToken))
                .andReturn());
    }

    @When("I request the price history for the missing product")
    public void iRequestThePriceHistoryForTheMissingProduct() throws Exception {
        context.setLastResult(mockMvc.perform(get("/products/{productId}/prices", context.getMissingProductId())
                .header("Authorization", "Bearer " + authToken))
                .andReturn());
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(context.getLastResult().getResponse().getStatus()).isEqualTo(status);
    }

    @And("the response should contain a generated product id")
    public void theResponseShouldContainAGeneratedProductId() throws Exception {
        JsonNode body = readLastResponseBody();
        assertThat(body.hasNonNull("productId")).isTrue();
        context.setCurrentProductId(UUID.fromString(body.get("productId").asText()));
    }

    @And("the response should contain a generated price id")
    public void theResponseShouldContainAGeneratedPriceId() throws Exception {
        JsonNode body = readLastResponseBody();
        assertThat(body.hasNonNull("id")).isTrue();
        context.setCurrentPriceId(UUID.fromString(body.get("id").asText()));
    }

    @And("the returned product should have name {string} and description {string}")
    public void theReturnedProductShouldHaveNameAndDescription(String name, String description) throws Exception {
        JsonNode body = readLastResponseBody();
        assertThat(body.get("id").asText()).isEqualTo(context.getCurrentProductId().toString());
        assertThat(body.get("name").asText()).isEqualTo(name);
        assertThat(body.get("description").asText()).isEqualTo(description);
    }

    @And("the effective price should be {string}")
    public void theEffectivePriceShouldBe(String expectedValue) throws Exception {
        JsonNode body = readLastResponseBody();
        assertThat(new BigDecimal(body.get("value").asText())).isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @And("the price history should contain {int} prices")
    public void thePriceHistoryShouldContainPrices(int expectedSize) throws Exception {
        JsonNode body = readLastResponseBody();
        assertThat(body.get("prices").size()).isEqualTo(expectedSize);
    }

    @And("price {int} in the history should have value {string}, init date {string} and end date {string}")
    public void priceInTheHistoryShouldHaveValueInitDateAndEndDate(int position, String value, String initDate, String endDate) throws Exception {
        JsonNode price = readLastResponseBody().get("prices").get(position - 1);
        assertThat(new BigDecimal(price.get("value").asText())).isEqualByComparingTo(new BigDecimal(value));
        assertThat(price.get("initDate").asText()).isEqualTo(initDate);
        if ("null".equalsIgnoreCase(endDate)) {
            assertThat(price.get("endDate").isNull()).isTrue();
        } else {
            assertThat(price.get("endDate").asText()).isEqualTo(endDate);
        }
    }

    @And("the error message should contain {string}")
    public void theErrorMessageShouldContain(String text) throws Exception {
        JsonNode body = readLastResponseBody();
        assertThat(body.get("message").asText()).contains(text);
    }

    private MvcResult createProduct(String name, String description) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("name", name);
        request.put("description", description);

        return mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andReturn();
    }

    private MvcResult addPrice(UUID productId, String value, LocalDate initDate, LocalDate endDate) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("value", new BigDecimal(value));
        request.put("initDate", initDate);
        request.put("endDate", endDate);

        return mockMvc.perform(post("/products/{productId}/prices", productId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andReturn();
    }

    private MvcResult updatePrice(UUID productId, UUID priceId, String value, LocalDate initDate, LocalDate endDate) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("value", new BigDecimal(value));
        request.put("initDate", initDate);
        request.put("endDate", endDate);

        return mockMvc.perform(put("/products/{productId}/prices/{priceId}", productId, priceId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andReturn();
    }

    private MvcResult deletePrice(UUID productId, UUID priceId) throws Exception {
        return mockMvc.perform(delete("/products/{productId}/prices/{priceId}", productId, priceId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private LocalDate parseNullableDate(String value) {
        return "null".equalsIgnoreCase(value) ? null : LocalDate.parse(value);
    }

    private JsonNode readLastResponseBody() throws Exception {
        return readBody(context.getLastResult());
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}

