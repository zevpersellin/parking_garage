package com.example.garage.controller.dto;

import com.example.garage.model.VehicleSize;

public record CheckInRequest(String licensePlate, VehicleSize size) {
}
