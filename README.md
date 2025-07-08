# Parking Garage Management API

A Spring Boot REST API for managing parking garage operations, including spot inventory management and vehicle tracking.

## Features

- **Spot Management**: View all parking spots and their current status
- **Vehicle Check-in/Check-out**: Assign cars to available spots and release them when leaving
- **Real-time Availability**: Get current list of available parking spots
- **Manual Spot Control**: Update spot status manually for maintenance or reservations
- **Comprehensive Error Handling**: Proper HTTP status codes and error messages

## Technology Stack

- **Java 17** - Modern LTS Java version
- **Spring Boot 3.3.1** - Web framework with embedded Tomcat
- **Maven 3.9+** - Build and dependency management
- **JUnit 5 + Mockito** - Testing framework
- **In-memory Storage** - ConcurrentHashMap for data persistence

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.9 or higher

### Running the Application

1. **Clone and navigate to the project directory**
   ```bash
   git clone <repository-url>
   cd parking_garage
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **The API will be available at**: `http://localhost:8080`

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
    "status": "AVAILABLE"
  },
  {
    "id": "A2",
    "level": 1,
    "number": 2,
    "status": "OCCUPIED"
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
    "status": "AVAILABLE"
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
  "status": "OCCUPIED"
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
  "licensePlate": "ABC-123"
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

**Response**: `200 OK` (Empty body)

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
  -d '{"licensePlate": "TEST-123"}'
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
- `status` (Enum): Either "AVAILABLE" or "OCCUPIED"

### Car
- `licensePlate` (String): Unique license plate identifier
- `assignedSpotId` (String): ID of the assigned parking spot
- `checkInAt` (Instant): Timestamp when the car was checked in

## Error Handling

The API uses standard HTTP status codes and returns consistent error responses:

- `200 OK` - Successful operation
- `201 CREATED` - Resource created successfully
- `404 NOT FOUND` - Resource not found
- `409 CONFLICT` - Business rule violation (e.g., garage full)

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

- **Unit Tests**: 8 tests covering service layer business logic
- **Integration Tests**: 6 tests covering full API workflows
- **Total Coverage**: 14 tests with 100% pass rate

### Test Categories

1. **Happy Path Tests**: Normal operation scenarios
2. **Error Handling Tests**: Exception and edge cases
3. **Business Logic Tests**: Garage full, car not found scenarios
4. **API Contract Tests**: HTTP status codes and response formats

## Future Enhancements

The current implementation provides a solid foundation for the following stretch goals:

- **Car Search**: `GET /cars/{licensePlate}` endpoint
- **Spot Size Compatibility**: Support for different vehicle and spot sizes
- **Billing System**: Hourly rate calculation based on parking duration
- **Persistent Storage**: Database integration for production use
- **Authentication**: User management and access control

## Development Notes

- The garage is initialized with 10 parking spots (A1-A10) on level 1
- All data is stored in-memory using ConcurrentHashMap for thread safety
- The application uses Java 17 records for immutable data models
- Error handling is centralized using Spring's @ControllerAdvice

## Contributing

1. Ensure all tests pass: `mvn test`
2. Follow the existing code style and patterns
3. Add tests for new functionality
4. Update this README for any API changes

---

*This project was developed as a demonstration of modern Spring Boot API development practices with comprehensive testing and documentation.*
