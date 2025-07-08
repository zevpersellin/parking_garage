package com.example.garage.service;

import com.example.garage.exception.CarNotFoundException;
import com.example.garage.exception.GarageFullException;
import com.example.garage.exception.SpotNotFoundException;
import com.example.garage.model.Car;
import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import com.example.garage.repository.CarRepository;
import com.example.garage.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private ParkingService parkingService;

    private ParkingSpot availableSpot;
    private ParkingSpot occupiedSpot;
    private Car testCar;

    @BeforeEach
    void setUp() {
        availableSpot = new ParkingSpot("A1", 1, 1, ParkingStatus.AVAILABLE);
        occupiedSpot = new ParkingSpot("A2", 1, 2, ParkingStatus.OCCUPIED);
        testCar = new Car("TEST-123", "A1", Instant.now());
    }

    @Test
    void getAllSpots_ShouldReturnAllSpots() {
        // Given
        List<ParkingSpot> spots = List.of(availableSpot, occupiedSpot);
        when(parkingSpotRepository.findAll()).thenReturn(spots);

        // When
        Collection<ParkingSpot> result = parkingService.getAllSpots();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(availableSpot, occupiedSpot);
        verify(parkingSpotRepository).findAll();
    }

    @Test
    void getAvailableSpots_ShouldReturnOnlyAvailableSpots() {
        // Given
        List<ParkingSpot> availableSpots = List.of(availableSpot);
        when(parkingSpotRepository.findAllAvailable()).thenReturn(availableSpots);

        // When
        Collection<ParkingSpot> result = parkingService.getAvailableSpots();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(availableSpot);
        verify(parkingSpotRepository).findAllAvailable();
    }

    @Test
    void updateSpotStatus_WithValidSpotId_ShouldUpdateAndReturnSpot() {
        // Given
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(availableSpot));
        ParkingSpot updatedSpot = new ParkingSpot("A1", 1, 1, ParkingStatus.OCCUPIED);
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(updatedSpot);

        // When
        ParkingSpot result = parkingService.updateSpotStatus("A1", ParkingStatus.OCCUPIED);

        // Then
        assertThat(result.status()).isEqualTo(ParkingStatus.OCCUPIED);
        assertThat(result.id()).isEqualTo("A1");
        verify(parkingSpotRepository).findById("A1");
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
    }

    @Test
    void updateSpotStatus_WithInvalidSpotId_ShouldThrowSpotNotFoundException() {
        // Given
        when(parkingSpotRepository.findById("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingService.updateSpotStatus("INVALID", ParkingStatus.OCCUPIED))
                .isInstanceOf(SpotNotFoundException.class)
                .hasMessage("Spot with id INVALID not found");
        
        verify(parkingSpotRepository).findById("INVALID");
        verify(parkingSpotRepository, never()).save(any());
    }

    @Test
    void checkIn_WithAvailableSpot_ShouldSuccessfullyCheckInCar() {
        // Given
        when(parkingSpotRepository.findFirstAvailable()).thenReturn(Optional.of(availableSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(occupiedSpot);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        Car result = parkingService.checkIn("TEST-123");

        // Then
        assertThat(result.licensePlate()).isEqualTo("TEST-123");
        assertThat(result.assignedSpotId()).isEqualTo("A1");
        assertThat(result.checkInAt()).isNotNull();
        
        verify(parkingSpotRepository).findFirstAvailable();
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void checkIn_WithNoAvailableSpots_ShouldThrowGarageFullException() {
        // Given
        when(parkingSpotRepository.findFirstAvailable()).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("TEST-123"))
                .isInstanceOf(GarageFullException.class)
                .hasMessage("No spots available");
        
        verify(parkingSpotRepository).findFirstAvailable();
        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void checkOut_WithValidLicensePlate_ShouldSuccessfullyCheckOutCar() {
        // Given
        when(carRepository.findByLicensePlate("TEST-123")).thenReturn(Optional.of(testCar));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableSpot);

        // When
        parkingService.checkOut("TEST-123");

        // Then
        verify(carRepository).findByLicensePlate("TEST-123");
        verify(parkingSpotRepository).findById("A1");
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(carRepository).deleteByLicensePlate("TEST-123");
    }

    @Test
    void checkOut_WithInvalidLicensePlate_ShouldThrowCarNotFoundException() {
        // Given
        when(carRepository.findByLicensePlate("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingService.checkOut("INVALID"))
                .isInstanceOf(CarNotFoundException.class)
                .hasMessage("Car with license plate INVALID not found");
        
        verify(carRepository).findByLicensePlate("INVALID");
        verify(parkingSpotRepository, never()).findById(anyString());
        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).deleteByLicensePlate(anyString());
    }

    @Test
    void findCarByLicensePlate_WithValidLicensePlate_ShouldReturnCar() {
        // Given
        when(carRepository.findByLicensePlate("TEST-123")).thenReturn(Optional.of(testCar));

        // When
        Car result = parkingService.findCarByLicensePlate("TEST-123");

        // Then
        assertThat(result).isEqualTo(testCar);
        assertThat(result.licensePlate()).isEqualTo("TEST-123");
        assertThat(result.assignedSpotId()).isEqualTo("A1");
        verify(carRepository).findByLicensePlate("TEST-123");
    }

    @Test
    void findCarByLicensePlate_WithInvalidLicensePlate_ShouldThrowCarNotFoundException() {
        // Given
        when(carRepository.findByLicensePlate("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingService.findCarByLicensePlate("INVALID"))
                .isInstanceOf(CarNotFoundException.class)
                .hasMessage("Car with license plate INVALID not found");
        
        verify(carRepository).findByLicensePlate("INVALID");
    }
}
