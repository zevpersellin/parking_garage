### **Checklist – Parking Garage API (Stretch Goals)**
*Version 1.5 | Last updated Jul 9 2025*
_For each box: ✓ = done, ➖ = not needed, ❗ = blocker._

---

### **Epic 1: Vehicle Search**
- [✓] **API:** Create `GET /cars/{licensePlate}` endpoint.
- [✓] **Logic:** Implement service method to find a car by its license plate.
- [✓] **Error Handling:** Return `404 Not Found` if the license plate does not exist in the system.
- [✓] **Testing:**
    - [✓] **Unit:** Test the service method for both found and not-found cases.
    - [✓] **Integration:** Add a test for the `GET` endpoint to verify `200 OK` and `404 Not Found` responses.

---

### **Epic 2: Spot & Vehicle Sizing**
- [✓] **Data Models:**
    - [✓] Add `size` attribute (Enum: `COMPACT`, `STANDARD`, `OVERSIZED`) to `ParkingSpot` model.
    - [✓] Add `size` attribute (Enum: `COMPACT`, `STANDARD`, `OVERSIZED`) to `Car` model.
- [✓] **API:** Modify `POST /cars/check-in` request body to accept the car's `size`.
- [✓] **Logic:** Update the check-in service to enforce size compatibility rules.
- [✓] **Error Handling:** Return `409 Conflict` if no compatible spot is available for the car's size.
- [✓] **Testing:**
    - [✓] **Unit:** Test the spot-finding logic with various car/spot size combinations.
    - [✓] **Integration:** Test the `check-in` endpoint to ensure it correctly assigns spots based on size and rejects incompatible requests.

---

### **Epic 3: Rate Calculation & Basic Billing**
- [✓] **Configuration:** Add a configurable hourly rate to `application.properties` (e.g., `parking.rate.hourly=5.00`).
- [✓] **Logic:**
    - [✓] Update the `check-out` service to calculate the parked duration.
    - [✓] Calculate the total fee based on the duration and the configured rate.
- [✓] **API:** Modify the `POST /cars/check-out` response to include the calculated `fee`.
- [✓] **Testing:**
    - [✓] **Unit:** Test the fee calculation logic, including edge cases (e.g., parking for less than an hour).
    - [✓] **Integration:** Test the `check-out` endpoint to verify the `fee` is present and correct in the JSON response.

---

### **Epic 4: Premium Spot Features**
- [✓] **Data Models:** Add a `features` attribute (e.g., `List<String>`) to the `ParkingSpot` model.
- [✓] **Configuration:** Add support for different rate configurations (e.g., `parking.rate.premium.ev=7.50`).
- [✓] **Logic:**
    - [✓] Update the rate calculation logic to check if a spot has premium features.
    - [✓] Apply the correct premium rate if a relevant feature is present.
- [✓] **Testing:**
    - [✓] **Unit:** Test the rate logic to ensure it correctly selects the premium rate over the standard rate when applicable.
    - [✓] **Integration:** Test a check-out from a spot with a premium feature to verify the higher fee is calculated and returned.

---

### **Final Review (Stretch Goals)**
- [✓] **Manual Verification:** All new and modified endpoints (`GET /cars/{plate}`, `POST /cars/check-in`, `POST /cars/check-out`) have been tested manually via `curl` or Postman.
- [✓] **Code Quality:** All new code is clean, well-structured, and follows the existing project patterns.
```
