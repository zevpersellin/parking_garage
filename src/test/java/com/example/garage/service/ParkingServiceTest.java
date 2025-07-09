package com.example.garage.service;

import com.example.garage.exception.*;
import com.example.garage.model.*;
import com.example.garage.repository.CarRepository;
import com.example.garage.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ParkingServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ParkingService parkingService;

    private ParkingSpot availableCompactSpot;
    private ParkingSpot availableStandardSpot;
    private ParkingSpot availableOversizedSpot;
    private ParkingSpot occupiedSpot;
    private Car testCar;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        availableCompactSpot = new ParkingSpot("A1", 1, 1, ParkingStatus.AVAILABLE, VehicleSize.COMPACT, List.of());
        availableStandardSpot = new ParkingSpot("B1", 2, 1, ParkingStatus.AVAILABLE, VehicleSize.STANDARD, List.of());
        availableOversizedSpot = new ParkingSpot("C1", 3, 1, ParkingStatus.AVAILABLE, VehicleSize.OVERSIZED, List.of("EV_CHARGING"));
        occupiedSpot = new ParkingSpot("A2", 1, 2, ParkingStatus.OCCUPIED, VehicleSize.COMPACT, List.of());
        
        // Setup mock clock
        fixedInstant = Instant.parse("2025-07-09T12:00:00Z");
        ZoneId defaultZone = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(defaultZone);

        testCar = new Car("TEST-123", "A1", fixedInstant, VehicleSize.COMPACT);

        ReflectionTestUtils.setField(parkingService, "hourlyRate", 5.00);
        ReflectionTestUtils.setField(parkingService, "premiumRate", 7.50);
    }

    @Test
    void getAllSpots_ShouldReturnAllSpots() {
        // Given
        List<ParkingSpot> spots = List.of(availableCompactSpot, occupiedSpot);
        when(parkingSpotRepository.findAll()).thenReturn(spots);

        // When
        Collection<ParkingSpot> result = parkingService.getAllSpots();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(availableCompactSpot, occupiedSpot);
        verify(parkingSpotRepository).findAll();
    }

    @Test
    void getAvailableSpots_ShouldReturnOnlyAvailableSpots() {
        // Given
        List<ParkingSpot> availableSpots = List.of(availableCompactSpot);
        when(parkingSpotRepository.findAllAvailable()).thenReturn(availableSpots);

        // When
        Collection<ParkingSpot> result = parkingService.getAvailableSpots();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(availableCompactSpot);
        verify(parkingSpotRepository).findAllAvailable();
    }

    @Test
    void updateSpotStatus_WithValidSpotId_ShouldUpdateAndReturnSpot() {
        // Given
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(availableCompactSpot));
        ParkingSpot updatedSpot = new ParkingSpot("A1", 1, 1, ParkingStatus.OCCUPIED, VehicleSize.COMPACT, List.of());
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
    void createParkingSpot_WithValidData_ShouldCreateAndReturnSpot() {
        // Given
        CreateSpotRequest request = new CreateSpotRequest("D1", 4, 1, VehicleSize.COMPACT, List.of());
        when(parkingSpotRepository.existsById("D1")).thenReturn(false);
        
        ParkingSpot expectedSpot = new ParkingSpot("D1", 4, 1, ParkingStatus.AVAILABLE, VehicleSize.COMPACT, List.of());
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(expectedSpot);

        // When
        ParkingSpot result = parkingService.createParkingSpot(request);

        // Then
        assertThat(result).isEqualTo(expectedSpot);
        verify(parkingSpotRepository).existsById("D1");
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
    }

    @Test
    void createParkingSpot_WhenSpotAlreadyExists_ShouldThrowSpotAlreadyExistsException() {
        // Given
        CreateSpotRequest request = new CreateSpotRequest("A1", 1, 1, VehicleSize.COMPACT, List.of());
        when(parkingSpotRepository.existsById("A1")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> parkingService.createParkingSpot(request))
                .isInstanceOf(SpotAlreadyExistsException.class)
                .hasMessage("Spot with id A1 already exists");

        verify(parkingSpotRepository).existsById("A1");
        verify(parkingSpotRepository, never()).save(any());
    }

    @Test
    void checkIn_WithAvailableCompactSpotForCompactCar_ShouldSucceed() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableCompactSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(occupiedSpot);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        Car result = parkingService.checkIn("TEST-123", VehicleSize.COMPACT);

        // Then
        assertThat(result.licensePlate()).isEqualTo("TEST-123");
        assertThat(result.assignedSpotId()).isEqualTo("A1");
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void checkIn_WithNullLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn(null, VehicleSize.COMPACT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(parkingSpotRepository, never()).findAllAvailable();
        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void checkIn_WithEmptyLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("", VehicleSize.COMPACT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(parkingSpotRepository, never()).findAllAvailable();
        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void checkIn_WithBlankLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("   ", VehicleSize.COMPACT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(parkingSpotRepository, never()).findAllAvailable();
        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void checkIn_WithNullVehicleSize_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("TEST-123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vehicle size cannot be null");

        verify(parkingSpotRepository, never()).findAllAvailable();
        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void checkIn_WithNoCompatibleSpot_ShouldThrowNoCompatibleSpotFoundException() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableCompactSpot));

        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("TEST-456", VehicleSize.OVERSIZED))
                .isInstanceOf(NoCompatibleSpotFoundException.class)
                .hasMessage("No compatible spot available for size OVERSIZED");

        verify(parkingSpotRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void checkIn_CompactCarInStandardSpot_ShouldSucceed() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableStandardSpot));
        ParkingSpot occupiedStandardSpot = new ParkingSpot("B1", 2, 1, ParkingStatus.OCCUPIED, VehicleSize.STANDARD, List.of());
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(occupiedStandardSpot);
        Car compactCarInStandardSpot = new Car("COMPACT-123", "B1", fixedInstant, VehicleSize.COMPACT);
        when(carRepository.save(any(Car.class))).thenReturn(compactCarInStandardSpot);

        // When
        Car result = parkingService.checkIn("COMPACT-123", VehicleSize.COMPACT);

        // Then
        assertThat(result.licensePlate()).isEqualTo("COMPACT-123");
        assertThat(result.assignedSpotId()).isEqualTo("B1");
        assertThat(result.size()).isEqualTo(VehicleSize.COMPACT);
    }

    @Test
    void checkIn_CompactCarInOversizedSpot_ShouldSucceed() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableOversizedSpot));
        ParkingSpot occupiedOversizedSpot = new ParkingSpot("C1", 3, 1, ParkingStatus.OCCUPIED, VehicleSize.OVERSIZED, List.of("EV_CHARGING"));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(occupiedOversizedSpot);
        Car compactCarInOversizedSpot = new Car("COMPACT-456", "C1", fixedInstant, VehicleSize.COMPACT);
        when(carRepository.save(any(Car.class))).thenReturn(compactCarInOversizedSpot);

        // When
        Car result = parkingService.checkIn("COMPACT-456", VehicleSize.COMPACT);

        // Then
        assertThat(result.licensePlate()).isEqualTo("COMPACT-456");
        assertThat(result.assignedSpotId()).isEqualTo("C1");
        assertThat(result.size()).isEqualTo(VehicleSize.COMPACT);
    }

    @Test
    void checkIn_StandardCarInOversizedSpot_ShouldSucceed() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableOversizedSpot));
        ParkingSpot occupiedOversizedSpot = new ParkingSpot("C1", 3, 1, ParkingStatus.OCCUPIED, VehicleSize.OVERSIZED, List.of("EV_CHARGING"));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(occupiedOversizedSpot);
        Car standardCarInOversizedSpot = new Car("STANDARD-789", "C1", fixedInstant, VehicleSize.STANDARD);
        when(carRepository.save(any(Car.class))).thenReturn(standardCarInOversizedSpot);

        // When
        Car result = parkingService.checkIn("STANDARD-789", VehicleSize.STANDARD);

        // Then
        assertThat(result.licensePlate()).isEqualTo("STANDARD-789");
        assertThat(result.assignedSpotId()).isEqualTo("C1");
        assertThat(result.size()).isEqualTo(VehicleSize.STANDARD);
    }

    @Test
    void checkIn_StandardCarCannotFitInCompactSpot_ShouldThrowException() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableCompactSpot));

        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("STANDARD-FAIL", VehicleSize.STANDARD))
                .isInstanceOf(NoCompatibleSpotFoundException.class)
                .hasMessage("No compatible spot available for size STANDARD");
    }

    @Test
    void checkIn_OversizedCarCannotFitInStandardSpot_ShouldThrowException() {
        // Given
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableStandardSpot));

        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("OVERSIZED-FAIL", VehicleSize.OVERSIZED))
                .isInstanceOf(NoCompatibleSpotFoundException.class)
                .hasMessage("No compatible spot available for size OVERSIZED");
    }

    @Test
    void checkOut_WithValidLicensePlate_ShouldSuccessfullyCheckOutCarAndReturnFee() {
        // Given
        Instant checkInTime = fixedInstant.minus(Duration.ofHours(2));
        Car carToCheckOut = new Car("TEST-123", "A1", checkInTime, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("TEST-123")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableCompactSpot);

        // When
        double fee = parkingService.checkOut("TEST-123");

        // Then
        assertThat(fee).isEqualTo(10.00); // 2 hours * $5.00/hr
        verify(carRepository).findByLicensePlate("TEST-123");
        verify(parkingSpotRepository).findById("A1");
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(carRepository).deleteByLicensePlate("TEST-123");
    }

    @Test
    void checkOut_WithPartialHour_ShouldCalculateProportionalFee() {
        // Given - 30 minutes parking
        Instant checkInTime = fixedInstant.minus(Duration.ofMinutes(30));
        Car carToCheckOut = new Car("PARTIAL-123", "A1", checkInTime, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("PARTIAL-123")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableCompactSpot);

        // When
        double fee = parkingService.checkOut("PARTIAL-123");

        // Then
        assertThat(fee).isEqualTo(2.50); // 0.5 hours * $5.00/hr
    }

    @Test
    void checkOut_WithVeryShortStay_ShouldCalculateMinimalFee() {
        // Given - 1 minute parking
        Instant checkInTime = fixedInstant.minus(Duration.ofMinutes(1));
        Car carToCheckOut = new Car("SHORT-123", "A1", checkInTime, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("SHORT-123")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableCompactSpot);

        // When
        double fee = parkingService.checkOut("SHORT-123");

        // Then
        assertThat(fee).isEqualTo(0.08); // 1/60 hours * $5.00/hr = 0.083... rounded to 0.08
    }

    @Test
    void checkOut_WithZeroDuration_ShouldReturnZeroFee() {
        // Given - same check-in and check-out time
        Car carToCheckOut = new Car("ZERO-123", "A1", fixedInstant, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("ZERO-123")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableCompactSpot);

        // When
        double fee = parkingService.checkOut("ZERO-123");

        // Then
        assertThat(fee).isEqualTo(0.00);
    }

    @Test
    void checkOut_FromPremiumSpot_ShouldCalculateHigherFee() {
        // Given
        Instant checkInTime = fixedInstant.minus(Duration.ofHours(1));
        Car carToCheckOut = new Car("PREMIUM-CAR", "C1", checkInTime, VehicleSize.OVERSIZED);
        ParkingSpot premiumSpot = new ParkingSpot("C1", 3, 1, ParkingStatus.OCCUPIED, VehicleSize.OVERSIZED, List.of("EV_CHARGING"));
        when(carRepository.findByLicensePlate("PREMIUM-CAR")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("C1")).thenReturn(Optional.of(premiumSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableOversizedSpot);

        // When
        double fee = parkingService.checkOut("PREMIUM-CAR");

        // Then
        assertThat(fee).isEqualTo(7.50); // 1 hour * $7.50/hr
    }

    @Test
    void checkOut_FromSpotWithMultipleFeatures_ShouldUseEVChargingRate() {
        // Given
        Instant checkInTime = fixedInstant.minus(Duration.ofHours(1));
        Car carToCheckOut = new Car("MULTI-FEATURE", "C1", checkInTime, VehicleSize.OVERSIZED);
        ParkingSpot multiFeatureSpot = new ParkingSpot("C1", 3, 1, ParkingStatus.OCCUPIED, VehicleSize.OVERSIZED, 
                List.of("EV_CHARGING", "COVERED", "SECURITY_CAMERA"));
        when(carRepository.findByLicensePlate("MULTI-FEATURE")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("C1")).thenReturn(Optional.of(multiFeatureSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableOversizedSpot);

        // When
        double fee = parkingService.checkOut("MULTI-FEATURE");

        // Then
        assertThat(fee).isEqualTo(7.50); // Should use premium rate for EV_CHARGING
    }

    @Test
    void checkOut_FromSpotWithoutEVCharging_ShouldUseStandardRate() {
        // Given
        Instant checkInTime = fixedInstant.minus(Duration.ofHours(1));
        Car carToCheckOut = new Car("NO-EV", "C2", checkInTime, VehicleSize.OVERSIZED);
        ParkingSpot nonPremiumSpot = new ParkingSpot("C2", 3, 2, ParkingStatus.OCCUPIED, VehicleSize.OVERSIZED, 
                List.of("COVERED", "SECURITY_CAMERA"));
        when(carRepository.findByLicensePlate("NO-EV")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("C2")).thenReturn(Optional.of(nonPremiumSpot));
        ParkingSpot availableNonPremiumSpot = new ParkingSpot("C2", 3, 2, ParkingStatus.AVAILABLE, VehicleSize.OVERSIZED, 
                List.of("COVERED", "SECURITY_CAMERA"));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableNonPremiumSpot);

        // When
        double fee = parkingService.checkOut("NO-EV");

        // Then
        assertThat(fee).isEqualTo(5.00); // Should use standard rate
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
    void checkOut_WithNullLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.checkOut(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(carRepository, never()).findByLicensePlate(anyString());
    }

    @Test
    void checkOut_WithEmptyLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.checkOut(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(carRepository, never()).findByLicensePlate(anyString());
    }

    @Test
    void checkOut_WithDataInconsistency_ShouldThrowIllegalStateException() {
        // Given
        Car carToCheckOut = new Car("INCONSISTENT", "NONEXISTENT", fixedInstant, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("INCONSISTENT")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingService.checkOut("INCONSISTENT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Assigned spot not found, data inconsistency");

        verify(carRepository).findByLicensePlate("INCONSISTENT");
        verify(parkingSpotRepository).findById("NONEXISTENT");
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

    @Test
    void findCarByLicensePlate_WithNullLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.findCarByLicensePlate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(carRepository, never()).findByLicensePlate(anyString());
    }

    @Test
    void findCarByLicensePlate_WithEmptyLicensePlate_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> parkingService.findCarByLicensePlate(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("License plate cannot be null or empty");

        verify(carRepository, never()).findByLicensePlate(anyString());
    }

    @Test
    void isCompatible_WithNullCarSize_ShouldReturnFalse() {
        // This tests the private method indirectly through checkIn
        when(parkingSpotRepository.findAllAvailable()).thenReturn(List.of(availableCompactSpot));

        // When & Then
        assertThatThrownBy(() -> parkingService.checkIn("TEST", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkOut_WithLongDuration_ShouldCalculateCorrectFee() {
        // Given - 25 hours parking (more than a day)
        Instant checkInTime = fixedInstant.minus(Duration.ofHours(25));
        Car carToCheckOut = new Car("LONG-STAY", "A1", checkInTime, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("LONG-STAY")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableCompactSpot);

        // When
        double fee = parkingService.checkOut("LONG-STAY");

        // Then
        assertThat(fee).isEqualTo(125.00); // 25 hours * $5.00/hr
    }

    @Test
    void checkOut_FeeRounding_ShouldRoundToTwoDecimalPlaces() {
        // Given - 7 minutes parking (should result in a fee that needs rounding)
        Instant checkInTime = fixedInstant.minus(Duration.ofMinutes(7));
        Car carToCheckOut = new Car("ROUNDING-TEST", "A1", checkInTime, VehicleSize.COMPACT);
        when(carRepository.findByLicensePlate("ROUNDING-TEST")).thenReturn(Optional.of(carToCheckOut));
        when(parkingSpotRepository.findById("A1")).thenReturn(Optional.of(occupiedSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableCompactSpot);

        // When
        double fee = parkingService.checkOut("ROUNDING-TEST");

        // Then
        // 7 minutes = 7/60 hours = 0.11666... hours
        // 0.11666... * $5.00 = $0.58333...
        // Should round to $0.58
        assertThat(fee).isEqualTo(0.58);
    }
}
