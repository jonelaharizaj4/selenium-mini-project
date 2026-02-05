# Selenium + Java Automation MiniProject 

---

## Architecture & Design
- **Page Object Model (POM)** for maintainability and separation of concerns
- **Explicit waits (WebDriverWait)** used throughout (no `Thread.sleep`)
- **Reusable helper utilities** for actions, waits, and assertions
- **TestNG lifecycle management** for setup/teardown
- **Failure handling via listeners** (automatic screenshot capture)

---

## Implemented Test Scenarios
1. Account registration + logout
2. Login with persisted credentials
3. Product hover style validation (CSS computed values)
4. Sale product pricing style validation (color + strikethrough)
5. Product filtering by color and price (style + data validation)
6. Sorting by price + wishlist validation
7. Cart flow: add items, update quantity, verify grand total
8. Cart cleanup and empty cart validation

---

## Project Structure
src/main/java
 ---- pages // Page Objects
 ---- helpers // Reusable logic (users, persistence, actions)
 ---- utils // Driver, waits, configuration

src/test/java
 ---- tests // Test classes
 ---- listeners // Screenshot on failure

---

## Execution
*** Prerequisites
- Java JDK 11+
- Maven
- Chrome browser

*** Run full test suite
```Open cmd to the root folder
mvn clean test

*** Run a single test independently
You can run any test class on its own using Maven:
```Open cmd to the root folder
mvn test -Dtest=RegistrationTest
mvn test -Dtest=LoginTest
mvn test -Dtest=HoverTest
mvn test -Dtest=SaleTest
mvn test -Dtest=CheckFiltersTest
mvn test -Dtest=SortingTest
mvn test -Dtest=ShoppingCartTest
mvn test -Dtest=EmptyShoppingCartTest

---

## Failure Handling
Screenshots are captured automatically on test failure
Artifacts are saved under target/


## Test Design Notes

Test 1 (Create Account) generates a valid user and persists the credentials locally.
Test 2 (Login) validates the login flow using the most recently registered user, without hard-coded credentials.

To support this, a lightweight User Persistence mechanism is used to store and reload the last registered user. This keeps authentication data realistic while preserving test independence.
Most other tests have Sign In as a precondition. For stability, these tests authenticate explicitly using a reusable register-and-stay-logged-in flow or by loading the last known user when available.

- Test 6 requires 'My Wish List (2 items)'.
To keep tests deterministic across repeated runs, the suite clears the wishlist at the start of Test 6 (if any items exist). This prevents leftover state from previous runs from affecting the expected wishlist count.

---

## Parallel Execution

When tests are executed in parallel, user consistency must be guaranteed to avoid data collisions.For this reason:
 User persistence is not used in parallel execution
 Each test must create and authenticate its own unique user
 Tests rely exclusively on the reusable registerUserLoggedIn flow, which:
  - generates a unique user per test
  - registers the user
  - keeps the session authenticated for the duration of the test


