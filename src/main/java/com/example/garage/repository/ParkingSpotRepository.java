package com.example.garage.repository;

import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import com.example.garage.model.VehicleSize;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Repository
public class ParkingSpotRepository {

    private final Map<String, ParkingSpot> parkingSpots = new ConcurrentHashMap<>();

    public ParkingSpotRepository() {
        // Level 1: Compact spots
        IntStream.rangeClosed(1, 5).forEach(i -> {
            String id = "A" + i;
            parkingSpots.put(id, new ParkingSpot(id, 1, i, ParkingStatus.AVAILABLE, VehicleSize.COMPACT, List.of()));
        });
        // Level 2: Standard spots
        IntStream.rangeClosed(1, 3).forEach(i -> {
            String id = "B" + i;
            parkingSpots.put(id, new ParkingSpot(id, 2, i, ParkingStatus.AVAILABLE, VehicleSize.STANDARD, List.of()));
        });
        // Level 3: Oversized spots
        IntStream.rangeClosed(1, 2).forEach(i -> {
            String id = "C" + i;
            List<String> features = (i == 1) ? List.of("EV_CHARGING") : List.of();
            parkingSpots.put(id, new ParkingSpot(id, 3, i, ParkingStatus.AVAILABLE, VehicleSize.OVERSIZED, features));
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
