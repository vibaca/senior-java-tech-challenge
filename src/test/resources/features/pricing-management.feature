Feature: Pricing management

  Scenario: Add a price to an existing product
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    When I add a price of "99.99" from "2024-01-01" to "2024-06-30" to the current product
    Then the response status should be 201
    And the response should contain a generated price id

  Scenario: Reject adding a price to a missing product
    Given a missing product id
    When I add a price of "99.99" from "2024-01-01" to "2024-06-30" to the missing product
    Then the response status should be 404
    And the error message should contain "Product not found"

  Scenario: Reject overlapping prices for the same product
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    And the current product has a price of "99.99" from "2024-01-01" to "2024-06-30"
    When I add a price of "129.99" from "2024-06-15" to "2024-12-31" to the current product
    Then the response status should be 409
    And the error message should contain "overlaps"

  Scenario: Reject a price with an invalid date range
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    When I add a price of "99.99" from "2024-06-30" to "2024-06-30" to the current product
    Then the response status should be 400
    And the error message should contain "initDate must be before endDate"

  Scenario: Reject a non-positive price value
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    When I add a price of "0.00" from "2024-01-01" to "2024-06-30" to the current product
    Then the response status should be 400
    And the error message should contain "price value must be greater than zero"

  Scenario: Get the effective price for a specific date
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    And the current product has a price of "99.99" from "2024-01-01" to "2024-06-30"
    And the current product has an open-ended price of "149.99" starting on "2024-07-01"
    When I request the effective price for the current product on "2024-08-15"
    Then the response status should be 200
    And the effective price should be "149.99"

  Scenario: Reject querying an effective price when none exists
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    And the current product has a price of "99.99" from "2024-01-01" to "2024-06-30"
    When I request the effective price for the current product on "2024-07-01"
    Then the response status should be 404
    And the error message should contain "No price found"

  Scenario: Reject querying an effective price for a missing product
    Given a missing product id
    When I request the effective price for the missing product on "2024-07-01"
    Then the response status should be 404
    And the error message should contain "No price found"

  Scenario: Get the full price history in chronological order
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    And the current product has a price of "99.99" from "2024-01-01" to "2024-06-30"
    And the current product has an open-ended price of "149.99" starting on "2024-07-01"
    When I request the price history for the current product
    Then the response status should be 200
    And the price history should contain 2 prices
    And price 1 in the history should have value "99.99", init date "2024-01-01" and end date "2024-06-30"
    And price 2 in the history should have value "149.99", init date "2024-07-01" and end date "null"

  Scenario: Reject querying price history for a missing product
    Given a missing product id
    When I request the price history for the missing product
    Then the response status should be 404
    And the error message should contain "Product not found"

