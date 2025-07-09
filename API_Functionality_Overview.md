# API Functionality Overview

This document outlines how the Parking Garage API handles core operational requirements.

### **1. Garage Layout**

*   **Manage floors and bays:** The current `ParkingSpot` model includes a `level` (floor) and a `number` attribute. While there isn't an explicit "bay" concept, the combination of `level` and `number` creates a unique location for each spot.
*   **Define and manage parking spots:** Each `ParkingSpot` has a unique `id` (e.g., "A1", "B5"), which is its primary identifier. The system initializes a fixed set of spots on startup.

### **2. Parking Spot Management**

Your API provides specific endpoints for this:

*   **List all parking spots with their availability status:**
    *   `GET /api/v1/spots`: Returns a list of all `ParkingSpot` objects, each including its current `status` (`AVAILABLE`, `OCCUPIED`, etc.).
*   **Retrieve only available spots:**
    *   `GET /api/v1/spots/available`: A dedicated endpoint that filters the list and returns only the spots with an `AVAILABLE` status.
*   **Ability to mark spots as occupied or available:**
    *   `PUT /api/v1/spots/{spotId}/status`: This allows for manually changing a spot's status. This is useful for maintenance or reserving a spot outside the normal check-in/check-out flow.
*   **Define and create new parking spots:**
    *   `POST /api/v1/spots`: Allows for creating a new parking spot by providing its details (`id`, `level`, `number`, `size`, `features`) in the request body. New spots are created with an `AVAILABLE` status by default.

### **3. Car Tracking**

The API manages car tracking through the following endpoints and logic:

*   **Check a car in:**
    *   `POST /api/v1/cars/check-in`: When you send a request with a `licensePlate` and `size`, the service finds the first available and compatible spot, marks it as `OCCUPIED`, and creates a `Car` record.
*   **Check a car out:**
    *   `POST /api/v1/cars/check-out`: This endpoint takes a `licensePlate`. The system finds the corresponding car, calculates the fee, frees up the associated parking spot by setting its status back to `AVAILABLE`, and then removes the car from the active list.
*   **Track check-in and check-out times:**
    *   The `Car` model has a `checkInAt` timestamp that is recorded the moment a car is successfully checked in.
    *   The check-out time is captured when the `check-out` endpoint is called. This time is used to calculate the duration of the stay for billing purposes.

---

### **4. Data Field and Feature Implementation**

This section details how the API's current implementation addresses specific data field and stretch goal requirements.

#### **Minimum Data Fields**

*   **Spot ID:** Implemented. The `ParkingSpot` model has a `String id` field (e.g., "A1").
*   **Floor:** Implemented. The `ParkingSpot` model has an `int level` field.
*   **Bay:** Not explicitly implemented. There is no `bay` field in the data model. A spot's location is uniquely identified by the combination of its `level` and `number`.
*   **Spot number:** Implemented. The `ParkingSpot` model has an `int number` field.
*   **Status (available/occupied):** Implemented. The `ParkingSpot` model has a `Status status` enum field with values like `AVAILABLE` and `OCCUPIED`.

*   **License plate number:** Implemented. The `Car` model has a `String licensePlate` field.
*   **Assigned spot ID:** Implemented. The `Car` model has a `String assignedSpotId` field.
*   **Check-in timestamp:** Implemented. The `Car` model has an `Instant checkInAt` field that is set upon check-in.

#### **Stretch Goals**

All listed stretch goals are confirmed as implemented in the API.

*   **Search:**
    *   **Status:** Complete.
    *   **Implementation:** The API provides a `GET /api/v1/cars/{licensePlate}` endpoint that allows looking up a car and its location by its license plate.

*   **Spot Types:**
    *   **Status:** Complete.
    *   **Implementation:** Both the `Car` and `ParkingSpot` models have been updated with a `VehicleSize size` attribute (Enum: `COMPACT`, `STANDARD`, `OVERSIZED`). The `POST /api/v1/cars/check-in` endpoint now requires a car's size and enforces compatibility rules, ensuring a vehicle can only park in a spot of the same size or larger.

*   **Rate Calculation:**
    *   **Status:** Complete.
    *   **Implementation:** The `POST /api/v1/cars/check-out` endpoint calculates a parking fee. The duration is determined by comparing the `checkInAt` timestamp to the current time, and the fee is calculated based on a configurable hourly rate in `application.properties`.

*   **Spot Features:**
    *   **Status:** Complete.
    *   **Implementation:** The `ParkingSpot` model now includes a `List<String> features`. The rate calculation logic checks this list for features like `"EV_CHARGING"` and applies a corresponding premium hourly rate if found.
