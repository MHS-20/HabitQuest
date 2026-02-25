Feature: User registration and authentication

  Scenario: Successful user registration
    Given a non-registered user
    When the user provides a valid email and password
    Then the system creates a new account
    And returns a valid authentication token

  Scenario: Successful login
    Given a registered user
    When the user provides correct credentials
    Then the system returns a valid JWT token

  Scenario: Login with invalid credentials
    Given a registered user
    When the user provides an incorrect password
    Then the system denies authentication
