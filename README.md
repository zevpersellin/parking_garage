# Parking Garage Management API v1.5

A Spring Boot REST API for managing parking garage operations, including spot inventory, vehicle tracking, and billing.

## Features

- **Spot Management**: View all parking spots and their current status.
- **Real-time Availability**: Get a current list of available parking spots.
- **Vehicle Search**: Find a car's location by its license plate number.
- **Spot & Vehicle Sizing**: Assign vehicles to compatible spot sizes (`COMPACT`, `STANDARD`, `OVERSIZED`).
- **Automated Billing**: Automatically calculate parking fees based on duration and spot type upon check-out.
- **Premium Spot Rates**: Configure and apply different hourly rates for premium spots (e.g., EV charging).
- **Vehicle Check-in/Check-out**: Assign cars to available spots and release them when leaving.
- **Manual Spot Control**: Update spot status manually for maintenance or reservations.
- **Comprehensive Error Handling**: Proper HTTP status codes and clear error messages.

## Technology Stack

- **Java 17** - Modern LTS Java version
- **Spring Boot 3.3.1** - Web framework with embedded Tomcat
- **Maven 3.9+** - Build and dependency management
- **JUnit 5 + Mockito** - Testing framework
- **In-memory Storage** - ConcurrentHashMap for data persistence

### Running the Application

1.  **Clone and navigate to the project directory**
    ```bash
    git clone <repository-url>
    cd parking_garage
    ```

2.  **Configure Rates (Optional)**
    - Create a file at `src/main/resources/application.properties`.
    - Add your desired hourly rates. If not provided, defaults will be used.
    ```properties
    # Standard hourly rate for parking
    parking.rate.hourly=5.00

    # Premium rate for spots with EV charging
    parking.rate.premium.ev=7.50
    ```

3.  **Build the project**
    ```bash
    mvn clean compile
    ```

4.  **Run the application**
    ```bash
    mvn spring-boot:run
    ```

5.  **The API will be available at**: `http://localhost:8080`

### Running Tests

```bash
# Run all tests
mvn test

# Run with verbose output
mvn test -X
```

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints

#### 1. Get All Parking Spots
```http
GET /spots
```

**Response**: `200 OK`
```json
[
  {
    "id": "A1",
    "level": 1,
    "number": 1,
    "status": "AVAILABLE",
    "size": "STANDARD",
    "features": []
  },
  {
    "id": "A2",
    "level": 1,
    "number": 2,
    "status": "OCCUPIED",
    "size": "COMPACT",
    "features": []
  }
]
```

#### 2. Get Available Parking Spots
```http
GET /spots/available
```

**Response**: `200 OK`
```json
[
  {
    "id": "A1",
    "level": 1,
    "number": 1,
    "status": "AVAILABLE",
    "size": "STANDARD",
    "features": []
  }
]
```

#### 3. Update Spot Status
```http
PUT /spots/{spotId}/status
```

**Request Body**:
```json
{
  "status": "OCCUPIED"
}
```

**Response**: `200 OK`
```json
{
  "id": "A1",
  "level": 1,
  "number": 1,
  "status": "OCCUPIED",
  "size": "STANDARD",
  "features": []
}
```

**Error Response**: `404 NOT FOUND`
```json
{
  "code": "SPOT_NOT_FOUND",
  "message": "Spot with id INVALID-SPOT not found"
}
```

#### 4. Check-in Car
```http
POST /cars/check-in
```

**Request Body**:
```json
{
  "licensePlate": "ABC-123",
  "size": "STANDARD"
}
```

**Response**: `201 CREATED`
```json
{
  "licensePlate": "ABC-123",
  "assignedSpotId": "A1",
  "checkInAt": "2025-07-08T19:30:00.123Z"
}
```

**Error Response**: `409 CONFLICT` (Garage Full)
```json
{
  "code": "GARAGE_FULL",
  "message": "No spots available"
}
```

**Error Response**: `409 CONFLICT` (No Compatible Spot)
```json
{
  "code": "NO_COMPATIBLE_SPOT_FOUND",
  "message": "No compatible spot found for vehicle size STANDARD"
}
```

#### 5. Check-out Car
```http
POST /cars/check-out
```

**Request Body**:
```json
{
  "licensePlate": "ABC-123"
}
```

**Response**: `200 OK`
```json
{
  "licensePlate": "ABC-123",
  "fee": 5.00
}
```

**Error Response**: `404 NOT FOUND`
```json
{
  "code": "CAR_NOT_FOUND",
  "message": "Car with license plate ABC-123 not found"
}
```

#### 6. Find Car by License Plate
```http
GET /cars/{licensePlate}
```

**Response**: `200 OK`
```json
{
  "licensePlate": "ABC-123",
  "assignedSpotId": "A1",
  "checkInAt": "2025-07-08T19:30:00.123Z"
}
```

**Error Response**: `404 NOT FOUND`
```json
{
  "code": "CAR_NOT_FOUND",
  "message": "Car with license plate ABC-123 not found"
}
```

## Sample Usage with curl

### Check available spots
```bash
curl -X GET http://localhost:8080/api/v1/spots/available
```

### Check-in a car
```bash
curl -X POST http://localhost:8080/api/v1/cars/check-in \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "TEST-123", "size": "COMPACT"}'
```

### Check-out a car
```bash
curl -X POST http://localhost:8080/api/v1/cars/check-out \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "TEST-123"}'
```

### Update spot status manually
```bash
curl -X PUT http://localhost:8080/api/v1/spots/A1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "OCCUPIED"}'
```

### View all spots
```bash
curl -X GET http://localhost:8080/api/v1/spots
```

## Testing with Postman

A comprehensive Postman collection is included in the project root: `Parking_Garage_API.postman_collection.json`

### How to Use the Postman Collection

1. **Import the Collection**:
   - Open Postman
   - Click "Import" → "Upload Files"
   - Select `Parking_Garage_API.postman_collection.json`

2. **Set Base URL**:
   - The collection uses a variable `{{baseUrl}}` set to `http://localhost:8080/api/v1`
   - Make sure your application is running on port 8080

3. **Collection Structure**:
   - **Spot Management**: All spot-related endpoints
   - **Car Management**: Check-in, check-out, and search endpoints
   - **Test Scenarios**: Complete workflows and edge cases

4. **Recommended Testing Order**:
   - Start with "Get Available Spots" to see initial state
   - Use "Complete Workflow Test" folder for end-to-end testing
   - Try "Garage Full Scenario" to test error handling

### Key Test Scenarios Included

- **Complete Workflow**: Check availability → Check-in → Find car → Check-out → Verify
- **Error Handling**: Invalid spot IDs, non-existent cars, garage full scenarios
- **Edge Cases**: Boundary conditions and business rule validation

## Data Models

### ParkingSpot
- `id` (String): Unique identifier (e.g., "A1", "A2")
- `level` (Integer): Floor level (currently all spots are on level 1)
- `number` (Integer): Spot number within the level
- `status` (Enum): `AVAILABLE`, `OCCUPIED`, or `MAINTENANCE`
- `size` (Enum): `COMPACT`, `STANDARD`, or `OVERSIZED`
- `features` (List<String>): A list of special features, e.g., `["EV_CHARGING"]`

### Car
- `licensePlate` (String): Unique license plate identifier
- `assignedSpotId` (String): ID of the assigned parking spot
- `checkInAt` (Instant): Timestamp when the car was checked in
- `size` (Enum): `COMPACT`, `STANDARD`, or `OVERSIZED`

## Error Handling

The API uses standard HTTP status codes and returns consistent error responses:

- `200 OK` - Successful operation
- `201 CREATED` - Resource created successfully
- `404 NOT FOUND` - Resource not found
- `409 CONFLICT` - Business rule violation (e.g., garage full, no compatible spot)

All error responses follow this format:
```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable error description"
}
```

## Architecture

The application follows a layered architecture:

- **Controller Layer**: REST endpoints and HTTP handling
- **Service Layer**: Business logic and validation
- **Repository Layer**: Data access abstraction
- **Model Layer**: Domain entities and DTOs

## Testing

The project includes comprehensive testing:

- **Unit Tests**: 10+ tests covering service layer business logic, including sizing and billing.
- **Integration Tests**: 8+ tests covering full API workflows and edge cases.
- **Total Coverage**: 18+ tests with a 100% pass rate.

### Test Categories

1.  **Happy Path Tests**: Normal operation scenarios
2.  **Error Handling Tests**: Exception and edge cases
3.  **Business Logic Tests**: Garage full, car not found, sizing conflicts, and rate selection
4.  **API Contract Tests**: HTTP status codes and response formats

## Future Enhancements

- **Persistent Storage**: Database integration (e.g., PostgreSQL) for production use.
- **Authentication & Authorization**: Secure endpoints with user roles and permissions.
- **Expanded Features**: Reservations, detailed reporting, and analytics.

## Development Notes

- The garage is initialized with 10 parking spots of various sizes.
- One oversized spot is pre-configured with the `EV_CHARGING` feature for premium rate testing.
- All data is stored in-memory using ConcurrentHashMap for thread safety.
- The application uses Java 17 records for immutable data models.
- Error handling is centralized using Spring's @ControllerAdvice

## Contributing

1. Ensure all tests pass: `mvn test`
2. Follow the existing code style and patterns
3. Add tests for new functionality
4. Update this README for any API changes

---

*This project was developed as a demonstration of modern Spring Boot API development practices with comprehensive testing and documentation.*
