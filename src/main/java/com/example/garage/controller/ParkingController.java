package com.example.garage.controller;

import com.example.garage.controller.dto.CheckInRequest;
import com.example.garage.controller.dto.CheckOutRequest;
import com.example.garage.controller.dto.UpdateSpotStatusRequest;
import com.example.garage.model.Car;
import com.example.garage.model.ParkingSpot;
import com.example.garage.service.ParkingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/spots")
    public Collection<ParkingSpot> getAllSpots() {
        return parkingService.getAllSpots();
    }

    @GetMapping("/spots/available")
    public Collection<ParkingSpot> getAvailableSpots() {
        return parkingService.getAvailableSpots();
    }

    @PutMapping("/spots/{id}/status")
    public ResponseEntity<ParkingSpot> updateSpotStatus(@PathVariable String id, @RequestBody UpdateSpotStatusRequest request) {
        ParkingSpot updatedSpot = parkingService.updateSpotStatus(id, request.status());
        return ResponseEntity.ok(updatedSpot);
    }

    @PostMapping("/cars/check-in")
    public ResponseEntity<Car> checkIn(@RequestBody CheckInRequest checkInRequest) {
        Car car = parkingService.checkIn(checkInRequest.licensePlate());
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }

    @PostMapping("/cars/check-out")
    public ResponseEntity<Void> checkOut(@RequestBody CheckOutRequest checkOutRequest) {
        parkingService.checkOut(checkOutRequest.licensePlate());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cars/{licensePlate}")
    public ResponseEntity<Car> findCar(@PathVariable String licensePlate) {
        Car car = parkingService.findCarByLicensePlate(licensePlate);
        return ResponseEntity.ok(car);
    }
}
