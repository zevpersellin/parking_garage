```markdown
# Architectural Guidelines (Exercise Scope)
*Version&nbsp;1.0 | Last updated Jul 8 2025*  
_Keep it small, finish on time._

---

## 1  Purpose
Provide just enough structure for a **Parking-Garage Management API** so reviewers can run the code, hit the endpoints, and see clean separation of concerns. Anything not required to meet the core user stories is **out of scope for this exercise**.

---

## 2  Technology Stack
| Layer            | Choice      | Why |
|------------------|-------------|-----|
| Language         | **Java 17** | LTS, modern features (records, switch patterns). |
| Framework        | **Spring Boot 3.x (Web MVC)** | Familiar, batteries-included; no need for extra config. |
| Build            | **Maven 3.9+** | Simple wrapper script, easy for graders. |
| Testing          | **JUnit 5 + Mockito** | Minimal dependencies, quick feedback. |

_No database or external services—use in-memory maps for persistence._

---

## 3  High-Level Architecture

```

HTTP ➜ ❚❚ Controller ❚❚ ➜ ❚❚ Service ❚❚ ➜ ❚❚ Repository (Map) ❚❚

``
* **Controller** – validates input, maps domain errors to HTTP status.  
* **Service** – business rules (find spot, check-in/out).  
* **Repository** – `ConcurrentHashMap` acting as fake DB (easy to switch later).

---

## 4  Domain Model (MVP Only)

| Entity        | Key Fields                                   |
|---------------|----------------------------------------------|
| `ParkingSpot` | `id`, `level`, `number`, `status` (`AVAILABLE`/`OCCUPIED`) |
| `Car`         | `licensePlate`, `assignedSpotId`, `checkInAt` |

_No billing, sizing, or reservations until stretch goals are approved._

---

## 5  API Surface (v1)

| Verb | Path                    | Purpose                 | Success / Error |
|------|-------------------------|-------------------------|-----------------|
| GET  | `/spots`                | List all spots          | `200 OK` |
| POST | `/cars/check-in`        | Assign first free spot  | `201 Created` / `409 Conflict` (garage full) |
| POST | `/cars/check-out`       | Release a spot          | `200 OK` / `404 Not Found` |

All responses are JSON; errors follow a simple envelope:

```json
{ "code": "GARAGE_FULL", "message": "No spots available" }
````

---

## 6  Error Handling

Create one `@ControllerAdvice` with methods like:

```java
@ExceptionHandler(GarageFullException.class)
@ResponseStatus(HttpStatus.CONFLICT)
```

This keeps controllers thin and avoids duplicate try/catch blocks.

---

## 7  Testing Strategy (Bare Minimum)

| Level       | What to cover                                                                            |
| ----------- | ---------------------------------------------------------------------------------------- |
| Unit        | `ParkingService` happy path + edge cases.                                                |
| Integration | `POST /cars/check-in` & `check-out` using `@SpringBootTest` WebEnvironment.RANDOM\_PORT. |

Aim for “tests prove it works,” **not** full coverage metrics.

---

## 8  Project Layout

```
src
 └─ main
     └─ java/com/example/garage
         ├─ controller
         ├─ service
         ├─ repository
         └─ model
 └─ test
     └─ java/com/example/garage
```

---

## 9  Out-of-Scope (Don’t Build Yet)

* Persistence layer (SQL/NoSQL)
* Authentication/authorization
* Pricing or billing logic
* Docker/CI pipelines
* Performance hardening

---

## 10  Revision History

| Ver | Date       | Notes            |
| --- | ---------- | ---------------- |
| 1.0 | Jul 8 2025 | First lean draft |

*End of file*

```
```
