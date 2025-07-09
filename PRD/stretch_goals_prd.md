
### **Product Requirements Document: Parking Garage API v1.5 (Advanced Features)**

* **Version:** 1.5
* **Status:** Proposed
* **Author:** Product Management
* **Last Updated:** July 9, 2025

---

### **1. Overview**

This document outlines the requirements for the v1.5 release of the Parking Garage Management API. Building on the core functionality of the MVP, this release focuses on introducing advanced features that will significantly enhance the operational efficiency and revenue potential for the garage operator. These features directly address the "Stretch Goals" identified in the initial project brief.

---

### **2. Themes & Goals**

The primary themes for this release are **Operational Intelligence** and **Monetization**.

| Goal                      | Success Metrics                                                                                        |
| :------------------------ | :----------------------------------------------------------------------------------------------------- |
| **Improve Operator Efficiency** | Reduce the time it takes to locate a customer's vehicle by 90% via a direct search feature.             |
| **Optimize Space Utilization** | Implement spot/vehicle size compatibility to ensure larger vehicles are not taking up smaller, more numerous spots. |
| **Introduce a Revenue Model** | Successfully calculate and return a parking fee upon vehicle check-out.                               |
| **Enable Premium Offerings** | Allow the operator to set different prices for spots with special features (e.g., EV charging).        |

---

### **3. Feature Requirements**

This section details the user stories and technical requirements for each of the new features.

#### **Epic 1: Vehicle Search**

* **User Story:** As a Garage Operator, a customer has lost their ticket and I need to find their car quickly, so I want to look up the car's location by its license plate number.
* **Requirements:**
    * Create a new API endpoint: `GET /cars/{licensePlate}`.
    * If a car with the specified `licensePlate` is found in the system, the endpoint must return a `200 OK` status with the full `Car` object, including its `assignedSpotId`.
    * If no car with that license plate is found, the endpoint must return a `404 Not Found` status.

#### **Epic 2: Spot & Vehicle Sizing**

* **User Story:** As a Garage Operator, I want to prevent large trucks from parking in compact-only spots so that I can maximize the number of vehicles my garage can hold.
* **Requirements:**
    * **Data Model Changes:**
        * The `ParkingSpot` model must be updated to include a `size` attribute (Enum: `COMPACT`, `STANDARD`, `OVERSIZED`).
        * The `Car` model must be updated to include a `size` attribute (Enum: `COMPACT`, `STANDARD`, `OVERSIZED`).
    * **Logic Changes:**
        * The `POST /cars/check-in` endpoint must be modified. The request body will now require both `licensePlate` and `size`.
        * The check-in logic must enforce compatibility:
            * A `COMPACT` car can be assigned to a `COMPACT`, `STANDARD`, or `OVERSIZED` spot.
            * A `STANDARD` car can be assigned to a `STANDARD` or `OVERSIZED` spot.
            * An `OVERSIZED` car can only be assigned to an `OVERSIZED` spot.
        * If no compatible spot is available, the API should return a `409 Conflict` status with a message indicating no suitable spots are free.

#### **Epic 3: Rate Calculation & Basic Billing**

* **User Story:** As a Garage Operator, I want the system to automatically calculate the parking fee when a customer leaves so that I can charge them accurately and efficiently.
* **Requirements:**
    * **Configuration:** The system must have a configurable hourly parking rate (e.g., stored in `application.properties`).
    * **Logic Changes:**
        * The `POST /cars/check-out` endpoint's logic must be updated.
        * Upon successful check-out, the system will calculate the duration of the stay (from `checkInTimestamp` to the current time).
        * The total fee must be calculated based on the duration and the configured hourly rate.
    * **API Response Changes:**
        * The response body for a successful `POST /cars/check-out` request must now include the calculated fee. Example: `{ "message": "Check-out successful", "licensePlate": "ABC-123", "fee": 15.00 }`.

#### **Epic 4: Premium Spot Features**

* **User Story:** As a Garage Operator, I want to charge a premium for my EV charging stations so I can capitalize on this valuable amenity.
* **Requirements:**
    * **Data Model Changes:**
        * The `ParkingSpot` model must be updated to include a list of `features` (e.g., `["EV_CHARGING"]`).
    * **Configuration:** The system must support different rate configurations (e.g., a base hourly rate and a premium EV charging rate).
    * **Logic Changes:**
        * The rate calculation logic in the `check-out` process must be updated.
        * If the spot the car occupied had the `EV_CHARGING` feature, the system must apply the premium rate instead of the base rate.
```