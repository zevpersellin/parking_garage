package com.example.garage.model;

import java.util.List;

public record CreateSpotRequest(
        String id,
        int level,
        int number,
        VehicleSize size,
        List<String> features
) {
}
