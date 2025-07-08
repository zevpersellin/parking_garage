package com.example.garage.service;

import com.example.garage.exception.CarNotFoundException;
import com.example.garage.exception.GarageFullException;
import com.example.garage.exception.SpotNotFoundException;
import com.example.garage.model.Car;
import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import com.example.garage.repository.CarRepository;
import com.example.garage.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;

@Service
public class ParkingService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final CarRepository carRepository;

    public ParkingService(ParkingSpotRepository parkingSpotRepository, CarRepository carRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.carRepository = carRepository;
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
        
        ParkingSpot updatedSpot = new ParkingSpot(spot.id(), spot.level(), spot.number(), status);
        return parkingSpotRepository.save(updatedSpot);
    }

    public synchronized Car checkIn(String licensePlate) {
        ParkingSpot spot = parkingSpotRepository.findFirstAvailable()
                .orElseThrow(() -> new GarageFullException("No spots available"));

        ParkingSpot updatedSpot = new ParkingSpot(spot.id(), spot.level(), spot.number(), ParkingStatus.OCCUPIED);
        parkingSpotRepository.save(updatedSpot);

        Car car = new Car(licensePlate, spot.id(), Instant.now());
        return carRepository.save(car);
    }

    public void checkOut(String licensePlate) {
        Car car = carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new CarNotFoundException("Car with license plate " + licensePlate + " not found"));

        ParkingSpot spot = parkingSpotRepository.findById(car.assignedSpotId())
                .orElseThrow(() -> new IllegalStateException("Assigned spot not found, data inconsistency"));

        ParkingSpot updatedSpot = new ParkingSpot(spot.id(), spot.level(), spot.number(), ParkingStatus.AVAILABLE);
        parkingSpotRepository.save(updatedSpot);

        carRepository.deleteByLicensePlate(licensePlate);
    }

    public Car findCarByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new CarNotFoundException("Car with license plate " + licensePlate + " not found"));
    }
}
