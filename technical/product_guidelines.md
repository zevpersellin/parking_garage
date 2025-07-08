### **Product Requirements Document: Parking Garage Management API v1.0**

* **Version:** 1.1
* **Status:** Proposed
* **Author:** Product Management
* **Last Updated:** July 8, 2025

---

### **1. Overview & Vision**

The Parking Garage Management API is a new initiative to provide a foundational, headless system for single-location garage operators. Our vision is to create a simple, reliable, and developer-friendly API that serves as the central nervous system for daily parking operations. This initial version (v1.0) will focus on delivering a Minimum Viable Product (MVP) that addresses the most critical operational needs: spot inventory management and vehicle tracking.

---

### **2. Problem Statement**

Our client, a parking garage owner, currently lacks a digital system to efficiently manage their facility. This leads to several operational challenges:

* **No Real-Time Visibility:** There is no quick way to determine how many spots are currently available, leading to inefficient use of space and potential lost revenue.
* **Manual Vehicle Tracking:** The process of tracking which car is in which spot is manual, slow, and prone to error.
* **Inefficient Operations:** Time is wasted manually checking floors and bays for open spots and logging vehicle check-ins and check-outs.

This API will solve these problems by providing a centralized, programmatic way to manage the garage's state, forming the backbone for any future operational tools or customer-facing applications.

---

### **3. Target Audience / Users**

The primary user of this API is the **Garage Operator** and their staff. The secondary user is the **Software Developer** who will build and integrate tools on top of this API. All requirements should be viewed through the lens of making their tasks simpler and more efficient.

---

### **4. Goals & Success Metrics (for this Exercise)**

For this exercise, success will be measured by how well the implementation meets the core technical and design objectives, rather than by production-level operational metrics.

| Goal | Success Metrics |
| :--- | :--- |
| **Demonstrate Core Functionality** | All core features (spot listing, check-in, check-out) are implemented and function correctly per the requirements. |
| **Code Clarity & Structure** | The codebase is well-organized, easy to read, and follows good software design principles. |
| **Functional Endpoints** | All API endpoints return the correct data structures and HTTP status codes in response to valid requests. |
| **Showcase Extensibility** | The design clearly allows for future enhancements (the "Stretch Goals") with minimal refactoring. |

---

### **5. User Stories & Requirements (MVP)**

This section outlines the core functionality for the v1.0 release.

#### **Epic: Garage & Spot Inventory Management**

* **User Story 1:** As a Garage Operator, I want to see a list of all my parking spots and their current status (available/occupied) so that I can have a complete overview of my garage's inventory.
    * **Requirement:** Create a `GET /spots` endpoint that returns a list of all `ParkingSpot` objects with all required data fields.
* **User Story 2:** As a Garage Operator, I need to quickly find an open spot for a new customer, so I want to retrieve a list of only the available spots.
    * [cite_start]**Requirement:** Create a `GET /spots/available` endpoint that returns a filtered list of `ParkingSpot` objects where the status is "available". [cite: 24]
* **User Story 3:** As a Garage Operator, I need to manually update a spot's status if a car parks without checking in or if a spot needs to be reserved, so I can ensure my data is accurate.
    * [cite_start]**Requirement:** Create a `PUT /spots/{spot_id}` endpoint to change the `status` of a specific parking spot. [cite: 25]

#### **Epic: Vehicle Tracking**

* **User Story 4:** As a Garage Operator, when a car arrives, I want to check it into the system so that it is assigned to a specific spot and its arrival time is logged.
    * **Requirement:** Create a `POST /cars/check-in` endpoint that takes a `license_plate_number`, finds an available spot, assigns the car to it, updates the spot's status to "occupied", and records the check-in timestamp.
* **User Story 5:** As a Garage Operator, when a car leaves, I want to check it out of the system so that its spot is marked as available for the next customer.
    * [cite_start]**Requirement:** Create a `POST /cars/check-out` endpoint that takes a `license_plate_number`, finds the car, and updates its assigned spot's status to "available". [cite: 29]

---

### **6. Data Models (MVP)**

Based on the requirements, the following data fields are mandatory for v1.0.

**`ParkingSpot` Object:**

* [cite_start]`spot_id` (String, Unique Identifier) [cite: 34]
* [cite_start]`floor` (Integer) [cite: 35]
* [cite_start]`bay` (String) [cite: 36]
* [cite_start]`spot_number` (Integer) [cite: 37]
* [cite_start]`status` (Enum: "available", "occupied") [cite: 38]

**`Car` Object:**

* [cite_start]`license_plate_number` (String, Unique Identifier) [cite: 40]
* [cite_start]`assigned_spot_id` (String, Foreign Key to ParkingSpot) [cite: 41]
* [cite_start]`check_in_timestamp` (ISO 8601 Datetime String) [cite: 42]

---

### **7. Out of Scope for MVP (v1.0)**

To ensure a focused and timely delivery, the following items are explicitly out of scope for this initial release:

* [cite_start]**Persistent Database:** All data will be stored in-memory. [cite: 55]
* [cite_start]**User Authentication/Authorization:** The API will be open and will not require login credentials. [cite: 57]
* **Billing and Payments:** No rate calculation or payment processing will be included.
* **Advanced Spot Features:** Support for different spot sizes or features like EV charging is not included.
* **Graphical User Interface (GUI):** This is a headless API-only product.

---

### **8. Future Enhancements (Post-MVP)**

The following features are valuable and are being considered for future releases. [cite_start]They are directly aligned with the "Stretch Goals" outlined in the project brief. [cite: 43]

* [cite_start]**Search:** Implement a `GET /cars/{license_plate}` endpoint to find a car's location. [cite: 45, 46]
* [cite_start]**Spot & Vehicle Typing:** Introduce `size` attributes for spots and cars to manage compatibility. [cite: 47, 48, 49]
* [cite_start]**Rate Calculation:** Add logic to calculate parking fees based on duration of stay. [cite: 50, 51]
* [cite_start]**Specialized Spot Features:** Add support for premium spots (e.g., EV charging) with different pricing models. [cite: 52, 53]