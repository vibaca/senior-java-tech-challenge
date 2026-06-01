Feature: Product management

  Scenario: Create a product successfully
    When I create a product named "Zapatillas deportivas" with description "Modelo 2025"
    Then the response status should be 201
    And the response should contain a generated product id

  Scenario: Retrieve an existing product
    Given a product named "Zapatillas deportivas" with description "Modelo 2025" exists
    When I retrieve the current product
    Then the response status should be 200
    And the returned product should have name "Zapatillas deportivas" and description "Modelo 2025"

  Scenario: Reject retrieving a missing product
    Given a missing product id
    When I retrieve the missing product
    Then the response status should be 404
    And the error message should contain "Product not found"

  Scenario: Reject creating a product with blank name
    When I create a product named "" with description "Modelo 2025"
    Then the response status should be 400
    And the error message should contain "name cannot be blank"

  Scenario: Reject creating a product with blank description
    When I create a product named "Zapatillas deportivas" with description ""
    Then the response status should be 400
    And the error message should contain "description cannot be blank"

