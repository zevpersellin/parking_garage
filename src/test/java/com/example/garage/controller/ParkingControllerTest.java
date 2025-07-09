package com.example.garage.controller;

import com.example.garage.exception.SpotAlreadyExistsException;
import com.example.garage.model.CreateSpotRequest;
import com.example.garage.model.ParkingSpot;
import com.example.garage.model.ParkingStatus;
import com.example.garage.model.VehicleSize;
import com.example.garage.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingService parkingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSpot_WhenSuccessful_ShouldReturn201Created() throws Exception {
        // Given
        CreateSpotRequest request = new CreateSpotRequest("D1", 4, 1, VehicleSize.COMPACT, List.of());
        ParkingSpot createdSpot = new ParkingSpot("D1", 4, 1, ParkingStatus.AVAILABLE, VehicleSize.COMPACT, List.of());

        when(parkingService.createParkingSpot(any(CreateSpotRequest.class))).thenReturn(createdSpot);

        // When & Then
        mockMvc.perform(post("/api/v1/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("D1"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createSpot_WhenSpotAlreadyExists_ShouldReturn409Conflict() throws Exception {
        // Given
        CreateSpotRequest request = new CreateSpotRequest("A1", 1, 1, VehicleSize.COMPACT, List.of());

        when(parkingService.createParkingSpot(any(CreateSpotRequest.class)))
                .thenThrow(new SpotAlreadyExistsException("Spot with id A1 already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SPOT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Spot with id A1 already exists"));
    }
}
