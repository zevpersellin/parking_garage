package com.example.garage.model;

import java.time.Instant;

public record Car(String licensePlate, String assignedSpotId, Instant checkInAt, VehicleSize size) {
}
