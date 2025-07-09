# Changelog - Parking Garage API v1.5

This changelog details the implementation of the advanced features ("Stretch Goals") for the Parking Garage API, completed on July 9, 2025.

---

### Epic 1: Vehicle Search

- **Status:** Verified Complete
- **Details:**
    - Confirmed the existence of the `GET /api/v1/cars/{licensePlate}` endpoint in `ParkingController`.
    - Confirmed the corresponding service method `findCarByLicensePlate` in `ParkingService` and repository method `findByLicensePlate` in `CarRepository`.
    - Verified that existing unit and integration tests cover the success (`200 OK`) and failure (`404 Not Found`) cases.

---

### Epic 2: Spot & Vehicle Sizing

- **Status:** Implemented
- **Details:**
    - **Data Models:**
        - Created `src/main/java/com/example/garage/model/VehicleSize.java` enum with values `COMPACT`, `STANDARD`, and `OVERSIZED`.
        - Updated the `Car` record (`Car.java`) to include a `VehicleSize size` attribute.
        - Updated the `ParkingSpot` record (`ParkingSpot.java`) to include a `VehicleSize size` attribute.
    - **API & Logic:**
        - Modified the `CheckInRequest` DTO (`CheckInRequest.java`) to require both `licensePlate` and `size`.
        - Updated the `checkIn` method in `ParkingService` to accept the vehicle size and implement compatibility rules. A car can park in a spot of its size or larger.
        - Added `findCompatibleSpot` and `isCompatible` private helper methods to `ParkingService` to encapsulate the spot-finding logic.
    - **Error Handling:**
        - Created the `NoCompatibleSpotFoundException.java` exception.
        - Added a handler for this exception to `GlobalExceptionHandler.java`, which returns a `409 Conflict` status with the code `NO_COMPATIBLE_SPOT_FOUND`.
    - **Testing:**
        - Added unit tests to `ParkingServiceTest.java` to verify the spot compatibility logic for various car and spot size combinations.
        - Added an integration test to `ParkingControllerIntegrationTest.java` to ensure the `check-in` endpoint correctly rejects requests when no compatible spot is available.
        - Updated all existing tests affected by the model and method signature changes.

---

### Epic 3: Rate Calculation & Basic Billing

- **Status:** Implemented
- **Details:**
    - **Configuration:**
        - Created `src/main/resources/application.properties`.
        - Added the configurable hourly rate: `parking.rate.hourly=5.00`.
    - **API & Logic:**
        - Created the `CheckOutResponse.java` DTO to include the `licensePlate` and calculated `fee`.
        - Injected the `parking.rate.hourly` value into `ParkingService` using the `@Value` annotation.
        - Updated the `checkOut` method in `ParkingService` to:
            1. Calculate the parked duration in minutes.
            2. Compute the total fee based on the duration and the hourly rate.
            3. Return the calculated fee.
        - Modified the `checkOut` endpoint in `ParkingController` to return the `CheckOutResponse` containing the fee.
    - **Testing:**
        - Updated `ParkingServiceTest` to test the fee calculation logic, using a mocked `Clock` to control time.
        - Updated `ParkingControllerIntegrationTest` to assert that the `fee` is present and correct in the checkout response.

---

### Epic 4: Premium Spot Features

- **Status:** Implemented
- **Details:**
    - **Data Models:**
        - Updated the `ParkingSpot` record (`ParkingSpot.java`) to include a `List<String> features` attribute to store premium features like `"EV_CHARGING"`.
    - **Configuration:**
        - Added a premium rate to `application.properties`: `parking.rate.premium.ev=7.50`.
    - **Logic:**
        - Injected the `parking.rate.premium.ev` value into `ParkingService`.
        - Updated the rate calculation logic in the `checkOut` method to check if a spot's `features` list contains `"EV_CHARGING"` and apply the premium rate if present.
    - **Data Initialization:**
        - Modified `ParkingSpotRepository.java` to initialize one of the oversized spots with the `"EV_CHARGING"` feature for testing purposes.
    - **Testing:**
        - Added a unit test to `ParkingServiceTest` to ensure the logic correctly selects the premium rate over the standard rate.
        - Added an integration test to `ParkingControllerIntegrationTest` to verify that checking out from a premium spot results in a higher fee.

---

### General & Test Refinements

- **Test Stability:**
    - Annotated `ParkingControllerIntegrationTest` with `@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)` to ensure a clean application state for each integration test, preventing cascading failures.
    - Refactored `ParkingService` to use an injectable `java.time.Clock`, allowing for precise control over time in tests.
    - Created a `TestClockConfig.java` to provide a fixed `Clock` bean for integration tests, making time-dependent calculations predictable and reliable.
    - Configured `ParkingServiceTest` with `@MockitoSettings(strictness = Strictness.LENIENT)` to prevent `UnnecessaryStubbingException` errors from mock setups in the `@BeforeEach` block.
- **Code Quality:**
    - Updated all affected constructor and method calls throughout the codebase to align with the new data models.
    - Ensured all new code follows existing project patterns and conventions.
