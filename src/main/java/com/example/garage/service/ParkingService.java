package com.example.garage.service;

import com.example.garage.exception.CarNotFoundException;
import com.example.garage.exception.GarageFullException;
import com.example.garage.exception.NoCompatibleSpotFoundException;
import com.example.garage.exception.SpotNotFoundException;
import com.example.garage.model.Car;
import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import com.example.garage.model.VehicleSize;
import com.example.garage.repository.CarRepository;
import com.example.garage.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

@Service
public class ParkingService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final CarRepository carRepository;

    @Value("${parking.rate.hourly:5.00}")
    private double hourlyRate;

    @Value("${parking.rate.premium.ev:7.50}")
    private double premiumRate;

    private final Clock clock;

    public ParkingService(ParkingSpotRepository parkingSpotRepository, CarRepository carRepository, Clock clock) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.carRepository = carRepository;
        this.clock = clock;
    }

    public Collection<ParkingSpot> getAllSpots() {
        return parkingSpotRepository.findAll();
    }

    public Collection<ParkingSpot> getAvailableSpots() {
        return parkingSpotRepository.findAllAvailable();
    }

    public ParkingSpot updateSpotStatus(String spotId, ParkingStatus status) {
        ParkingSpot spot = parkingSpotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("Spot with id " + spotId + " not found"));
        
        ParkingSpot updatedSpot = new ParkingSpot(spot.id(), spot.level(), spot.number(), status, spot.size(), spot.features());
        return parkingSpotRepository.save(updatedSpot);
    }

    public synchronized Car checkIn(String licensePlate, VehicleSize size) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        if (size == null) {
            throw new IllegalArgumentException("Vehicle size cannot be null");
        }

        ParkingSpot spot = findCompatibleSpot(size)
                .orElseThrow(() -> new NoCompatibleSpotFoundException("No compatible spot available for size " + size));

        ParkingSpot updatedSpot = new ParkingSpot(spot.id(), spot.level(), spot.number(), ParkingStatus.OCCUPIED, spot.size(), spot.features());
        parkingSpotRepository.save(updatedSpot);

        Car car = new Car(licensePlate, spot.id(), Instant.now(clock), size);
        return carRepository.save(car);
    }

    public double checkOut(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }

        Car car = carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new CarNotFoundException("Car with license plate " + licensePlate + " not found"));

        ParkingSpot spot = parkingSpotRepository.findById(car.assignedSpotId())
                .orElseThrow(() -> new IllegalStateException("Assigned spot not found, data inconsistency"));

        ParkingSpot updatedSpot = new ParkingSpot(spot.id(), spot.level(), spot.number(), ParkingStatus.AVAILABLE, spot.size(), spot.features());
        parkingSpotRepository.save(updatedSpot);

        carRepository.deleteByLicensePlate(licensePlate);

        long durationMinutes = Duration.between(car.checkInAt(), Instant.now(clock)).toMinutes();
        double rate = spot.features().contains("EV_CHARGING") ? premiumRate : hourlyRate;
        double fee = (durationMinutes / 60.0) * rate;
        return Math.round(fee * 100.0) / 100.0;
    }

    public Car findCarByLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }

        return carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new CarNotFoundException("Car with license plate " + licensePlate + " not found"));
    }

    private java.util.Optional<ParkingSpot> findCompatibleSpot(VehicleSize size) {
        return parkingSpotRepository.findAllAvailable().stream()
                .filter(spot -> isCompatible(size, spot.size()))
                .findFirst();
    }

    private boolean isCompatible(VehicleSize carSize, VehicleSize spotSize) {
        if (carSize == null || spotSize == null) {
            return false;
        }
        switch (carSize) {
            case COMPACT:
                return true; // Compact cars fit in any spot
            case STANDARD:
                return spotSize == VehicleSize.STANDARD || spotSize == VehicleSize.OVERSIZED;
            case OVERSIZED:
                return spotSize == VehicleSize.OVERSIZED;
            default:
                return false;
        }
    }
}
