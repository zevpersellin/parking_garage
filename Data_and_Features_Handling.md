# Data and Feature Handling Analysis

This document details how the API's current implementation addresses specific data field and stretch goal requirements.

---

### **2. Minimum Data Fields**

#### **2a. For Parking Spots**

*   **Spot ID:** Implemented. The `ParkingSpot` model has a `String id` field (e.g., "A1").
*   **Floor:** Implemented. The `ParkingSpot` model has an `int level` field.
*   **Bay:** Not explicitly implemented. There is no `bay` field in the data model. A spot's location is uniquely identified by the combination of its `level` and `number`.
*   **Spot number:** Implemented. The `ParkingSpot` model has an `int number` field.
*   **Status (available/occupied):** Implemented. The `ParkingSpot` model has a `Status status` enum field with values like `AVAILABLE` and `OCCUPIED`.

#### **2b. For Cars**

*   **License plate number:** Implemented. The `Car` model has a `String licensePlate` field.
*   **Assigned spot ID:** Implemented. The `Car` model has a `String assignedSpotId` field.
*   **Check-in timestamp:** Implemented. The `Car` model has an `Instant checkInAt` field that is set upon check-in.

---

### **3. Stretch Goals**

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
