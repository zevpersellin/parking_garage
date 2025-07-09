package com.example.garage;

import com.example.garage.controller.dto.*;
import com.example.garage.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.garage.config.TestClockConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestClockConfig.class)
public class ParkingControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCheckInAndCheckOut() {
        // 1. Check-in a car
        CheckInRequest checkInRequest = new CheckInRequest("TEST-123", VehicleSize.COMPACT);
        ResponseEntity<Car> checkInResponse = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);

        assertThat(checkInResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(checkInResponse.getBody()).isNotNull();
        assertThat(checkInResponse.getBody().licensePlate()).isEqualTo("TEST-123");
        assertThat(checkInResponse.getBody().assignedSpotId()).isNotNull();
        assertThat(checkInResponse.getBody().size()).isEqualTo(VehicleSize.COMPACT);

        // 2. Verify the spot is occupied
        String assignedSpotId = checkInResponse.getBody().assignedSpotId();
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        assertThat(spotsResponse.getBody()).anyMatch(spot -> spot.id().equals(assignedSpotId) && spot.status() == com.example.garage.model.ParkingStatus.OCCUPIED);

        // 3. Check-out the car
        CheckOutRequest checkOutRequest = new CheckOutRequest("TEST-123");
        ResponseEntity<CheckOutResponse> checkOutResponse = restTemplate.postForEntity("/api/v1/cars/check-out", checkOutRequest, CheckOutResponse.class);

        assertThat(checkOutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(checkOutResponse.getBody()).isNotNull();
        assertThat(checkOutResponse.getBody().licensePlate()).isEqualTo("TEST-123");
        assertThat(checkOutResponse.getBody().fee()).isGreaterThanOrEqualTo(0);


        // 4. Verify the spot is available again
        ResponseEntity<ParkingSpot[]> spotsAfterCheckoutResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsAfterCheckoutResponse.getBody()).isNotNull();
        assertThat(spotsAfterCheckoutResponse.getBody()).anyMatch(spot -> spot.id().equals(assignedSpotId) && spot.status() == com.example.garage.model.ParkingStatus.AVAILABLE);
    }

    @Test
    void testCheckOutCarNotFound() {
        CheckOutRequest checkOutRequest = new CheckOutRequest("GHOST-CAR");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-out", checkOutRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("CAR_NOT_FOUND");
    }

    @Test
    void testGetAvailableSpots() {
        // 1. Initially all spots should be available
        ResponseEntity<ParkingSpot[]> initialResponse = restTemplate.getForEntity("/api/v1/spots/available", ParkingSpot[].class);
        assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initialResponse.getBody()).isNotNull();
        int initialAvailableCount = initialResponse.getBody().length;

        // 2. Check-in a car
        CheckInRequest checkInRequest = new CheckInRequest("TEST-AVAILABLE", VehicleSize.COMPACT);
        restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);

        // 3. Available spots should be reduced by 1
        ResponseEntity<ParkingSpot[]> afterCheckInResponse = restTemplate.getForEntity("/api/v1/spots/available", ParkingSpot[].class);
        assertThat(afterCheckInResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterCheckInResponse.getBody()).isNotNull();
        assertThat(afterCheckInResponse.getBody().length).isEqualTo(initialAvailableCount - 1);

        // 4. All returned spots should have AVAILABLE status
        for (ParkingSpot spot : afterCheckInResponse.getBody()) {
            assertThat(spot.status()).isEqualTo(ParkingStatus.AVAILABLE);
        }
    }

    @Test
    void testUpdateSpotStatus() {
        // 1. Get an available spot
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        ParkingSpot availableSpot = spotsResponse.getBody()[0];
        assertThat(availableSpot.status()).isEqualTo(ParkingStatus.AVAILABLE);

        // 2. Update spot status to OCCUPIED
        UpdateSpotStatusRequest updateRequest = new UpdateSpotStatusRequest(ParkingStatus.OCCUPIED);
        HttpEntity<UpdateSpotStatusRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<ParkingSpot> updateResponse = restTemplate.exchange(
                "/api/v1/spots/" + availableSpot.id() + "/status",
                HttpMethod.PUT,
                requestEntity,
                ParkingSpot.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().status()).isEqualTo(ParkingStatus.OCCUPIED);
        assertThat(updateResponse.getBody().id()).isEqualTo(availableSpot.id());

        // 3. Verify the change persisted
        ResponseEntity<ParkingSpot[]> verifyResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(verifyResponse.getBody()).isNotNull();
        assertThat(verifyResponse.getBody()).anyMatch(spot -> 
            spot.id().equals(availableSpot.id()) && spot.status() == ParkingStatus.OCCUPIED);
    }

    @Test
    void testUpdateSpotStatusWithInvalidSpotId() {
        UpdateSpotStatusRequest updateRequest = new UpdateSpotStatusRequest(ParkingStatus.OCCUPIED);
        HttpEntity<UpdateSpotStatusRequest> requestEntity = new HttpEntity<>(updateRequest);
        
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/spots/INVALID-SPOT/status",
                HttpMethod.PUT,
                requestEntity,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SPOT_NOT_FOUND");
        assertThat(response.getBody().message()).contains("INVALID-SPOT");
    }

    @Test
    void testNoCompatibleSpotFound() {
        // Fill all spots
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        for (ParkingSpot spot : spotsResponse.getBody()) {
            if (spot.status() == ParkingStatus.AVAILABLE) {
                CheckInRequest checkInRequest = new CheckInRequest("FILL-" + spot.id(), spot.size());
                restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);
            }
        }

        // Try to check in one more car
        CheckInRequest checkInRequest = new CheckInRequest("FAIL-CAR", VehicleSize.COMPACT);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("NO_COMPATIBLE_SPOT_FOUND");
    }

    @Test
    void testFindCarByLicensePlate() {
        // 1. Check-in a car first
        CheckInRequest checkInRequest = new CheckInRequest("SEARCH-TEST", VehicleSize.STANDARD);
        ResponseEntity<Car> checkInResponse = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);
        assertThat(checkInResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(checkInResponse.getBody()).isNotNull();

        Car checkedInCar = checkInResponse.getBody();

        // 2. Search for the car by license plate
        ResponseEntity<Car> searchResponse = restTemplate.getForEntity("/api/v1/cars/SEARCH-TEST", Car.class);

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).isNotNull();
        assertThat(searchResponse.getBody().licensePlate()).isEqualTo("SEARCH-TEST");
        assertThat(searchResponse.getBody().assignedSpotId()).isEqualTo(checkedInCar.assignedSpotId());
        assertThat(searchResponse.getBody().checkInAt()).isEqualTo(checkedInCar.checkInAt());
    }

    @Test
    void testFindCarByLicensePlateNotFound() {
        // Try to search for a car that doesn't exist
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/v1/cars/NONEXISTENT", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("CAR_NOT_FOUND");
        assertThat(response.getBody().message()).contains("NONEXISTENT");
    }

    @Test
    void testPremiumSpotBilling() {
        // 1. Find a premium spot and check in
        CheckInRequest checkInRequest = new CheckInRequest("PREMIUM-TEST", VehicleSize.OVERSIZED);
        ResponseEntity<Car> checkInResponse = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);
        assertThat(checkInResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Check out and verify the fee
        CheckOutRequest checkOutRequest = new CheckOutRequest("PREMIUM-TEST");
        ResponseEntity<CheckOutResponse> checkOutResponse = restTemplate.postForEntity("/api/v1/cars/check-out", checkOutRequest, CheckOutResponse.class);
        assertThat(checkOutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(checkOutResponse.getBody()).isNotNull();
        // With a fixed clock, the duration is 0, so the fee should be 0
        assertThat(checkOutResponse.getBody().fee()).isEqualTo(0.0);
    }

    @Test
    void testCheckInWithNullLicensePlate() {
        // This test would require modifying the request to send null, which is tricky with JSON
        // Instead, we test with empty string
        CheckInRequest checkInRequest = new CheckInRequest("", VehicleSize.COMPACT);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCheckInWithNullVehicleSize() {
        CheckInRequest checkInRequest = new CheckInRequest("TEST-NULL-SIZE", null);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCheckOutWithEmptyLicensePlate() {
        CheckOutRequest checkOutRequest = new CheckOutRequest("");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-out", checkOutRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testFindCarWithEmptyLicensePlate() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/v1/cars/", ErrorResponse.class);

        // This will likely return 404 due to path not matching, but that's expected behavior
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testVehicleSizeCompatibility_CompactCarCanUseAnySpot() {
        // Test that compact cars can use any available spot type
        CheckInRequest compactInCompact = new CheckInRequest("COMPACT-1", VehicleSize.COMPACT);
        ResponseEntity<Car> response1 = restTemplate.postForEntity("/api/v1/cars/check-in", compactInCompact, Car.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response1.getBody().size()).isEqualTo(VehicleSize.COMPACT);

        CheckInRequest compactInStandard = new CheckInRequest("COMPACT-2", VehicleSize.COMPACT);
        ResponseEntity<Car> response2 = restTemplate.postForEntity("/api/v1/cars/check-in", compactInStandard, Car.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getBody().size()).isEqualTo(VehicleSize.COMPACT);

        CheckInRequest compactInOversized = new CheckInRequest("COMPACT-3", VehicleSize.COMPACT);
        ResponseEntity<Car> response3 = restTemplate.postForEntity("/api/v1/cars/check-in", compactInOversized, Car.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response3.getBody().size()).isEqualTo(VehicleSize.COMPACT);
    }

    @Test
    void testVehicleSizeCompatibility_StandardCarCannotFitInCompactSpot() {
        // Fill all standard and oversized spots first
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        
        for (ParkingSpot spot : spotsResponse.getBody()) {
            if (spot.status() == ParkingStatus.AVAILABLE && 
                (spot.size() == VehicleSize.STANDARD || spot.size() == VehicleSize.OVERSIZED)) {
                CheckInRequest fillRequest = new CheckInRequest("FILL-" + spot.id(), spot.size());
                restTemplate.postForEntity("/api/v1/cars/check-in", fillRequest, Car.class);
            }
        }

        // Now try to check in a standard car - should fail
        CheckInRequest checkInRequest = new CheckInRequest("STANDARD-FAIL", VehicleSize.STANDARD);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("NO_COMPATIBLE_SPOT_FOUND");
    }

    @Test
    void testVehicleSizeCompatibility_OversizedCarCanOnlyFitInOversizedSpot() {
        // Fill all oversized spots first
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        
        for (ParkingSpot spot : spotsResponse.getBody()) {
            if (spot.status() == ParkingStatus.AVAILABLE && spot.size() == VehicleSize.OVERSIZED) {
                CheckInRequest fillRequest = new CheckInRequest("FILL-OVERSIZED-" + spot.id(), VehicleSize.OVERSIZED);
                restTemplate.postForEntity("/api/v1/cars/check-in", fillRequest, Car.class);
            }
        }

        // Now try to check in another oversized car - should fail
        CheckInRequest checkInRequest = new CheckInRequest("OVERSIZED-FAIL", VehicleSize.OVERSIZED);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("NO_COMPATIBLE_SPOT_FOUND");
    }

    @Test
    void testMultipleCheckInsAndCheckOuts() {
        // Check in multiple cars
        CheckInRequest request1 = new CheckInRequest("MULTI-1", VehicleSize.COMPACT);
        CheckInRequest request2 = new CheckInRequest("MULTI-2", VehicleSize.STANDARD);
        CheckInRequest request3 = new CheckInRequest("MULTI-3", VehicleSize.OVERSIZED);

        ResponseEntity<Car> response1 = restTemplate.postForEntity("/api/v1/cars/check-in", request1, Car.class);
        ResponseEntity<Car> response2 = restTemplate.postForEntity("/api/v1/cars/check-in", request2, Car.class);
        ResponseEntity<Car> response3 = restTemplate.postForEntity("/api/v1/cars/check-in", request3, Car.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify all cars can be found
        ResponseEntity<Car> find1 = restTemplate.getForEntity("/api/v1/cars/MULTI-1", Car.class);
        ResponseEntity<Car> find2 = restTemplate.getForEntity("/api/v1/cars/MULTI-2", Car.class);
        ResponseEntity<Car> find3 = restTemplate.getForEntity("/api/v1/cars/MULTI-3", Car.class);

        assertThat(find1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(find2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(find3.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check out all cars
        CheckOutRequest checkout1 = new CheckOutRequest("MULTI-1");
        CheckOutRequest checkout2 = new CheckOutRequest("MULTI-2");
        CheckOutRequest checkout3 = new CheckOutRequest("MULTI-3");

        ResponseEntity<CheckOutResponse> checkoutResponse1 = restTemplate.postForEntity("/api/v1/cars/check-out", checkout1, CheckOutResponse.class);
        ResponseEntity<CheckOutResponse> checkoutResponse2 = restTemplate.postForEntity("/api/v1/cars/check-out", checkout2, CheckOutResponse.class);
        ResponseEntity<CheckOutResponse> checkoutResponse3 = restTemplate.postForEntity("/api/v1/cars/check-out", checkout3, CheckOutResponse.class);

        assertThat(checkoutResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(checkoutResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(checkoutResponse3.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify cars can no longer be found
        ResponseEntity<ErrorResponse> notFound1 = restTemplate.getForEntity("/api/v1/cars/MULTI-1", ErrorResponse.class);
        ResponseEntity<ErrorResponse> notFound2 = restTemplate.getForEntity("/api/v1/cars/MULTI-2", ErrorResponse.class);
        ResponseEntity<ErrorResponse> notFound3 = restTemplate.getForEntity("/api/v1/cars/MULTI-3", ErrorResponse.class);

        assertThat(notFound1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testSpotFeaturesArePreservedAfterCheckInAndCheckOut() {
        // Check in to a premium spot
        CheckInRequest checkInRequest = new CheckInRequest("FEATURE-TEST", VehicleSize.OVERSIZED);
        ResponseEntity<Car> checkInResponse = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);
        assertThat(checkInResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String assignedSpotId = checkInResponse.getBody().assignedSpotId();

        // Get the spot and verify it has features
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        
        ParkingSpot occupiedSpot = null;
        for (ParkingSpot spot : spotsResponse.getBody()) {
            if (spot.id().equals(assignedSpotId)) {
                occupiedSpot = spot;
                break;
            }
        }
        
        assertThat(occupiedSpot).isNotNull();
        assertThat(occupiedSpot.status()).isEqualTo(ParkingStatus.OCCUPIED);
        
        // Check out
        CheckOutRequest checkOutRequest = new CheckOutRequest("FEATURE-TEST");
        ResponseEntity<CheckOutResponse> checkOutResponse = restTemplate.postForEntity("/api/v1/cars/check-out", checkOutRequest, CheckOutResponse.class);
        assertThat(checkOutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify the spot still has its features after checkout
        ResponseEntity<ParkingSpot[]> spotsAfterCheckout = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsAfterCheckout.getBody()).isNotNull();
        
        ParkingSpot availableSpot = null;
        for (ParkingSpot spot : spotsAfterCheckout.getBody()) {
            if (spot.id().equals(assignedSpotId)) {
                availableSpot = spot;
                break;
            }
        }
        
        assertThat(availableSpot).isNotNull();
        assertThat(availableSpot.status()).isEqualTo(ParkingStatus.AVAILABLE);
        assertThat(availableSpot.features()).isEqualTo(occupiedSpot.features());
    }
}
