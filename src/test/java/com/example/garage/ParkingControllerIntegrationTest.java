package com.example.garage;

import com.example.garage.controller.dto.CheckInRequest;
import com.example.garage.controller.dto.CheckOutRequest;
import com.example.garage.controller.dto.ErrorResponse;
import com.example.garage.controller.dto.UpdateSpotStatusRequest;
import com.example.garage.model.Car;
import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ParkingControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCheckInAndCheckOut() {
        // 1. Check-in a car
        CheckInRequest checkInRequest = new CheckInRequest("TEST-123");
        ResponseEntity<Car> checkInResponse = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);

        assertThat(checkInResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(checkInResponse.getBody()).isNotNull();
        assertThat(checkInResponse.getBody().licensePlate()).isEqualTo("TEST-123");
        assertThat(checkInResponse.getBody().assignedSpotId()).isNotNull();

        // 2. Verify the spot is occupied
        String assignedSpotId = checkInResponse.getBody().assignedSpotId();
        ResponseEntity<ParkingSpot[]> spotsResponse = restTemplate.getForEntity("/api/v1/spots", ParkingSpot[].class);
        assertThat(spotsResponse.getBody()).isNotNull();
        assertThat(spotsResponse.getBody()).anyMatch(spot -> spot.id().equals(assignedSpotId) && spot.status() == com.example.garage.model.ParkingStatus.OCCUPIED);

        // 3. Check-out the car
        CheckOutRequest checkOutRequest = new CheckOutRequest("TEST-123");
        restTemplate.postForEntity("/api/v1/cars/check-out", checkOutRequest, Void.class);

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
        CheckInRequest checkInRequest = new CheckInRequest("TEST-AVAILABLE");
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
    void testGarageFullScenario() {
        // 1. Check how many spots are currently available
        ResponseEntity<ParkingSpot[]> initialAvailableResponse = restTemplate.getForEntity("/api/v1/spots/available", ParkingSpot[].class);
        assertThat(initialAvailableResponse.getBody()).isNotNull();
        int availableSpots = initialAvailableResponse.getBody().length;

        // 2. Fill all remaining available spots
        for (int i = 1; i <= availableSpots; i++) {
            CheckInRequest checkInRequest = new CheckInRequest("FULL-CAR-" + i);
            ResponseEntity<Car> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, Car.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        // 3. Try to check in one more car - should fail
        CheckInRequest checkInRequest = new CheckInRequest("OVERFLOW-CAR");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/v1/cars/check-in", checkInRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("GARAGE_FULL");
        assertThat(response.getBody().message()).isEqualTo("No spots available");

        // 4. Verify no available spots
        ResponseEntity<ParkingSpot[]> availableResponse = restTemplate.getForEntity("/api/v1/spots/available", ParkingSpot[].class);
        assertThat(availableResponse.getBody()).isNotNull();
        assertThat(availableResponse.getBody()).hasSize(0);
    }

    @Test
    void testFindCarByLicensePlate() {
        // 1. Check-in a car first
        CheckInRequest checkInRequest = new CheckInRequest("SEARCH-TEST");
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
}
