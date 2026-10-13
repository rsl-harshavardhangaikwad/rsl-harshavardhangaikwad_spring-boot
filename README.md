# Leave Management API

A RESTful Leave Management API built with **Spring Boot 3.3** for an organization's HR system.
Employees can submit and manage leave requests through clean, validated REST endpoints.

---

## Project Overview

| Detail | Value |
|---|---|
| Framework | Spring Boot 3.3.4 |
| Language | Java 21 |
| Build Tool | Maven 3.x |
| Storage | In-memory (`ConcurrentHashMap`) |
| Testing | JUnit 5, Mockito, MockMvc |

### Architecture

The application follows a strict **layered architecture**:

```
HTTP Request
    │
    ▼
┌─────────────────────────────┐
│  Controller (LeaveController) │  ← REST layer, input validation (@Valid)
└─────────────┬───────────────┘
              │ Constructor DI
              ▼
┌─────────────────────────────┐
│  Service (LeaveServiceImpl)  │  ← Business logic, rule enforcement
└─────────────┬───────────────┘
              │ Constructor DI
              ▼
┌────────────────────────────────────┐
│  Repository (InMemoryLeaveRepository)│  ← Data access (ConcurrentHashMap)
└────────────────────────────────────┘
```

### Package Structure

```
src/main/java/com/rsl/leavemanagement/
├── LeaveManagementApplication.java   # Spring Boot entry point
├── configuration/
│   ├── AppConfig.java                # Bean definitions, @EnableConfigurationProperties
│   └── AppProperties.java            # Type-safe config bound from application.yml
├── controller/
│   └── LeaveController.java          # REST endpoints (/leaves)
├── dto/
│   ├── LeaveRequestDto.java          # Incoming payload with Bean Validation
│   ├── LeaveResponseDto.java         # Outgoing response shape
│   └── ErrorResponseDto.java         # Unified error response shape
├── exception/
│   ├── GlobalExceptionHandler.java   # @RestControllerAdvice
│   ├── LeaveNotFoundException.java   # 404 – leave not found
│   └── InvalidLeaveOperationException.java  # 409 – business rule violation
├── model/
│   ├── LeaveRequest.java             # Domain entity with Builder pattern
│   ├── LeaveType.java                # Enum: ANNUAL, SICK, CASUAL, …
│   └── LeaveStatus.java              # Enum: PENDING, APPROVED, REJECTED, CANCELLED
├── repository/
│   ├── LeaveRepository.java          # Repository interface
│   └── InMemoryLeaveRepository.java  # ConcurrentHashMap + AtomicLong implementation
└── service/
    ├── LeaveService.java             # Service interface
    └── LeaveServiceImpl.java         # Business logic implementation
```

---

## Build & Run Instructions

### Prerequisites

- Java 21+
- Apache Maven 3.6+

### Clone & Build

```bash
git clone <your-repo-url>
cd leave-management

# Compile and package
mvn clean package -DskipTests

# Run with the 'dev' profile (default)
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/leave-management-1.0.0.jar
```

The server starts on **`http://localhost:8080`**.

### Spring Profiles

| Profile | Description |
|---|---|
| `dev` (default) | Verbose DEBUG logging, request payload logging enabled |
| (none / prod) | WARN-level root logging only |

Switch profile:
```bash
# Via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Via JAR
java -Dspring.profiles.active=prod -jar target/leave-management-1.0.0.jar
```

---

## API Endpoint Summary

Base URL: `http://localhost:8080`

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| `GET` | `/leaves` | Get all leave requests | — | `200 OK` + JSON array |
| `GET` | `/leaves/{id}` | Get leave request by ID | — | `200 OK` + JSON object |
| `POST` | `/leaves` | Create a new leave request | `LeaveRequestDto` | `201 Created` + JSON object |
| `PUT` | `/leaves/{id}` | Update an existing leave request | `LeaveRequestDto` | `200 OK` + JSON object |
| `DELETE` | `/leaves/{id}` | Delete a leave request | — | `204 No Content` |

### Request Body Schema (`LeaveRequestDto`)

```json
{
  "employeeId": "EMP001",
  "leaveType": "ANNUAL",
  "startDate": "2026-11-01",
  "endDate": "2026-11-05",
  "reason": "Family vacation planned for the school holidays"
}
```

**Supported `leaveType` values:** `ANNUAL`, `SICK`, `CASUAL`, `MATERNITY`, `PATERNITY`, `UNPAID`, `COMPENSATORY`

### Response Body Schema (`LeaveResponseDto`)

```json
{
  "id": 1,
  "employeeId": "EMP001",
  "leaveType": "ANNUAL",
  "startDate": "2026-11-01",
  "endDate": "2026-11-05",
  "reason": "Family vacation planned for the school holidays",
  "status": "PENDING"
}
```

**Supported `status` values:** `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`

### Sample cURL Requests

```bash
# Create a leave request
curl -X POST http://localhost:8080/leaves \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "EMP001",
    "leaveType": "ANNUAL",
    "startDate": "2026-11-01",
    "endDate": "2026-11-05",
    "reason": "Family vacation planned for the school holidays"
  }'

# Get all leave requests
curl http://localhost:8080/leaves

# Get leave request by ID
curl http://localhost:8080/leaves/1

# Update a leave request
curl -X PUT http://localhost:8080/leaves/1 \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "EMP001",
    "leaveType": "CASUAL",
    "startDate": "2026-11-03",
    "endDate": "2026-11-07",
    "reason": "Personal work requiring my presence at home town"
  }'

# Delete a leave request
curl -X DELETE http://localhost:8080/leaves/1
```

### Error Response Schema

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/leaves",
  "timestamp": "2026-10-13T14:30:00",
  "validationErrors": {
    "employeeId": "Employee ID must not be blank",
    "reason": "Reason must be between 10 and 500 characters"
  }
}
```

---

## Business Rules

| Rule | Detail |
|---|---|
| Default Status | All new leave requests are created with `PENDING` status |
| Update Restriction | Only `PENDING` requests can be updated |
| Delete Restriction | `APPROVED` requests cannot be deleted (contact HR) |
| Date Validation | `endDate` must not be before `startDate` |

---

## Testing Instructions

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
# Service layer unit tests only
mvn test -Dtest=LeaveServiceImplTest

# Controller MockMvc tests only
mvn test -Dtest=LeaveControllerTest
```

### Test Summary

| Test Class | Type | Tests | Coverage |
|---|---|---|---|
| `LeaveServiceImplTest` | Unit (Mockito) | 12 | Service business logic |
| `LeaveControllerTest` | Integration (MockMvc) | 12 | REST endpoints + validation |
| **Total** | | **24** | |

### Test Coverage Areas

**Service Tests** (`LeaveServiceImplTest`):
- `getAllLeaves()` — empty list + populated list
- `getLeaveById()` — found + not found (exception)
- `createLeave()` — success + end-before-start date validation
- `updateLeave()` — PENDING success + APPROVED block + not found
- `deleteLeave()` — PENDING success + APPROVED block + not found

**Controller Tests** (`LeaveControllerTest`):
- `GET /leaves` — 200 with data + 200 with empty list
- `GET /leaves/{id}` — 200 found + 404 not found
- `POST /leaves` — 201 created + 400 blank employeeId + 400 short reason + 400 null leaveType
- `PUT /leaves/{id}` — 200 updated + 404 not found
- `DELETE /leaves/{id}` — 204 success + 404 not found

---

## Key Spring Boot Features Demonstrated

| Feature | Implementation |
|---|---|
| **Constructor DI** | `LeaveController`, `LeaveServiceImpl` use constructor injection only |
| **Bean Validation** | `@NotBlank`, `@NotNull`, `@Size`, `@FutureOrPresent`, `@Future` on `LeaveRequestDto` |
| **Global Exception Handling** | `@RestControllerAdvice` in `GlobalExceptionHandler` |
| **Configuration Properties** | `AppProperties` record bound from `application.yml` |
| **Spring Profiles** | `dev` profile with enhanced logging in `application-dev.yml` |
| **DTO Pattern** | Separate `LeaveRequestDto` (input) and `LeaveResponseDto` (output) |
| **Repository Pattern** | Interface `LeaveRepository` → `InMemoryLeaveRepository` implementation |
| **Structured Logging** | SLF4J with DEBUG/INFO levels throughout all layers |
