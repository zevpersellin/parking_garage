package com.example.garage.model;

import java.util.List;

public record ParkingSpot(String id, int level, int number, ParkingStatus status, VehicleSize size, List<String> features) {
}
