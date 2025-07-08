package com.example.garage.controller.dto;

import com.example.garage.model.ParkingStatus;

public record UpdateSpotStatusRequest(ParkingStatus status) {
}
