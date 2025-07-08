package com.example.garage.repository;

import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Repository
public class ParkingSpotRepository {

    private final Map<String, ParkingSpot> parkingSpots = new ConcurrentHashMap<>();

    public ParkingSpotRepository() {
        // Initialize with 10 parking spots on level 1
        IntStream.rangeClosed(1, 10).forEach(i -> {
            String id = "A" + i;
            parkingSpots.put(id, new ParkingSpot(id, 1, i, ParkingStatus.AVAILABLE));
        });
    }

    public Collection<ParkingSpot> findAll() {
        return parkingSpots.values();
    }

    public Optional<ParkingSpot> findFirstAvailable() {
        return parkingSpots.values().stream()
                .filter(spot -> spot.status() == ParkingStatus.AVAILABLE)
                .findFirst();
    }

    public Collection<ParkingSpot> findAllAvailable() {
        return parkingSpots.values().stream()
                .filter(spot -> spot.status() == ParkingStatus.AVAILABLE)
                .toList();
    }

    public ParkingSpot save(ParkingSpot spot) {
        parkingSpots.put(spot.id(), spot);
        return spot;
    }

    public Optional<ParkingSpot> findById(String id) {
        return Optional.ofNullable(parkingSpots.get(id));
    }
}
