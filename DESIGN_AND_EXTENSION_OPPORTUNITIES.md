# Parking Garage API

## 1  High-Level Design Choices & Trade-Offs

| Area | Current Choice | Why It Works | Trade-Offs |
|------|----------------|-------------|------------|
| **Framework** | Spring Boot 3, Java 17 | Opinionated, batteries-included, wide community, native records support | Heavier than Micronaut/Quarkus; cold-start time |
| **Data Layer** | In-memory `ConcurrentHashMap` repositories | Zero-config, fast unit tests, easy to grok | Volatile, single-instance only, limited querying |
| **Domain Modelling** | Java records (`ParkingSpot`, `Car`) | Immutable, concise, value semantics | No lazy setters; needs constructor inflation for partial updates |
| **Sizing Rules** | Enum `VehicleSize` + helper compatibility function | Simple, extensible | Hard-coded matrix; adding new sizes needs code change |
| **Billing** | Flat hourly & optional premium rate in `application.properties` | Simple to reason about & test | No pro-rating, discounts, taxes, or currencies |
| **Error Handling** | `@RestControllerAdvice` with typed exceptions → `ErrorResponse` | Single surface for client errors | More exception classes as scope grows |
| **Testing** | JUnit 5 + Mockito + MockMvc + Integration tests | Fast feedback, full stack covered | Integration tests still start Tomcat; might be slow in large CI |

## 2  Extension Opportunities

1. **Persistence Layer**
   * Swap `ConcurrentHashMap` with Spring Data repository (JPA/Mongo).  
   * Introduce Flyway for schema versioning.
2. **Multi-Garage / Multi-Level Modeling**  
   * Add `garageId`, `bay` concepts.  
   * Move compatibility logic to a _Strategy_ pattern so each garage can plug its own rules.
3. **Reservations & Pre-booking**  
   * New entity `Reservation` with TTL.  
   * Concurrency: optimistic locking on `ParkingSpot` row.
4. **Dynamic Pricing / Promotions**  
   * PricingService interface → implementations (flat, tiered, demand-based).  
   * Factor in loyalty points, coupons.
5. **Authentication & RBAC**  
   * Spring Security + JWT.  
   * Roles: `ATTENDANT`, `MANAGER`, `CUSTOMER`.
6. **Event-Driven Architecture**  
   * Publish `CarCheckedIn/Out`, `SpotCreated` events to Kafka for analytics, billing, IoT displays.
7. **Operator Dashboard**  
   * WebSocket endpoint streaming real-time availability.
8. **Scalability**  
   * Package as Docker image, deploy to K8s, leverage horizontal pod autoscaling.  
   * Externalize state to Redis/Postgres.
9. **Observability**  
   * Micrometer metrics (`spots.available`, `checkins.perMinute`).  
   * Structured JSON logging with correlation IDs.
10. **Mobile SDK**  
    * Expose GraphQL endpoint for flexible querying by mobile apps.

## 3  Creative Improvement Ideas

| Idea | Rationale | Rough Implementation Sketch |
|------|-----------|-----------------------------|
| **License-Plate OCR Integration** | Seamless driver experience—camera auto-checks car in/out. | Edge device calls `POST /cars/check-in` with plate; also verifies via ML service. |
| **EV Charger Load Balancing** | Optimise power draw & monetise charging. | Extend `features` with `CHARGER_KW`; scheduler assigns cars based on battery level. |
| **Dynamic Surge Pricing** | Match demand (concert nights) → revenue. | Cron job updates config or uses `PricingService` strategy via rules engine (e.g., Drools). |
| **Climate Impact Scoring** | Encourage smaller cars / EVs. | Store `fuelType`; pricing discounts for eco-friendly vehicles; dashboard KPI. |
| **Occupancy Prediction** | Help city plan traffic. | Train model on historical check-in data, expose `/forecast?date=` endpoint. |

---

### Conclusion
The current design favours **simplicity, testability, and rapid iteration**. By isolating core logic in the service layer and keeping data access abstract, we have a clear path to evolve into a production-grade system—whether that means persistent storage, richer pricing, or large-scale multi-garage operations. The outlined extension points and creative ideas aim to spark discussion on how this platform can grow with business needs.
