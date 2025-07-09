```markdown
# Checklist – Parking Garage API (Exercise Scope)  
*Version 1.1 | Last updated Jul 8 2025*  
_For each box: ✓ = done, ➖ = not needed, ❗ = blocker._

---

## 1  Project Setup
- [x] Init Maven 3.9 project with Spring Boot 3.x **(Java 17)**.  
- [x] Add dependencies: `spring-boot-starter-web`, `spring-boot-starter-test`.  
- [x] Configure in-memory storage (simple `ConcurrentHashMap` bean).  
- [x] Disable auth/security; JSON in/out only.

---

## 2  Data Models (MVP)
- [x] `ParkingSpot` → `id`, `level`, `number`, `status`.  
- [x] `Car` → `licensePlate`, `assignedSpotId`, `checkInAt`.  
_All fields implemented as Java 17 records._

---

## 3  Core Endpoints (v1)
| API | Done? |
|-----|-------|
| `GET /spots` – list all spots | [x] |
| `GET /spots/available` – list free spots | [x] |
| `POST /cars/check-in` – assign spot | [x] |
| `POST /cars/check-out` – free spot | [x] |
| `PUT /spots/{id}/status` – update spot status | [x] |

_All return JSON; errors use envelope `{code,message}`._

---

## 4  Business Logic
- [x] Service finds **first** free spot; throws `GarageFullException` if none.  
- [x] On check-out, service validates car exists; else `CarNotFoundException`.  
- [x] Repository layer hides storage details.

---

## 5  Error Handling
- [x] Global `@ControllerAdvice` maps domain exceptions → HTTP codes (`409`, `404`).  
- [x] Common error payload structure implemented once.

---

## 6  Testing (lean)
- [✓] **Unit** – `ParkingService` happy & full-garage paths (10 tests).  
- [✓] **Integration** – comprehensive endpoint testing using `@SpringBootTest` (8 tests).  
- [✓] Tests pass with `mvn test` (18 tests, 0 failures).

---

## 7  Manual Review
- [✓] Code compiles & runs: `mvn spring-boot:run`.  
- [✓] Endpoints verified via `curl` - all functionality tested successfully.  
- [✓] README includes quick-start and sample requests.

---

## 8  Stretch Goals (only if time remains)
- [✓] `GET /cars/{plate}` search.  
- [ ] Spot size compatibility.  
- [ ] Basic hourly billing.

---

## 9  Demo-Day Ready?
- [ ] Repo pushed & clean.  
- [ ] Five-minute walkthrough rehearsed (design choices, AI workflow).  
- [ ] Slide deck stubbed (max 6 slides).

---

*Lean list adapted from original checklist to focus on must-haves and finish on time.* :contentReference[oaicite:2]{index=2}
```
