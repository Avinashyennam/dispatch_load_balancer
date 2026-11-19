# FreightFox Dispatch Load Balancer

A Spring Boot REST API that allocates delivery orders to available vehicles based on vehicle capacity, order priority, and minimum additional travel distance.

The application uses a **priority-first, cheapest-insertion allocation heuristic** and the **Haversine formula** to calculate distances between geographical coordinates.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Allocation Algorithm](#allocation-algorithm)
- [Distance Calculation](#distance-calculation)
- [API Documentation](#api-documentation)
- [Running Locally](#running-locally)
- [Testing](#testing)
- [Performance Benchmark](#performance-benchmark)
- [Design Decisions and Limitations](#design-decisions-and-limitations)

## Overview

The Dispatch Load Balancer solves a delivery allocation problem:

- Multiple delivery orders need to be assigned to a fleet of vehicles.
- Each vehicle has a maximum carrying capacity and a starting location.
- Each order has a package weight, delivery location, and priority.
- Orders must be assigned without exceeding vehicle capacity.
- Higher-priority orders must be considered before lower-priority orders.
- The allocation should minimize the additional travel distance.

The application exposes REST APIs to upload orders and vehicle information and retrieve the generated dispatch plan.

## Features

- **Order Management:** Accepts and stores delivery orders with location, package weight, and priority.
- **Vehicle Management:** Accepts and stores vehicles with capacity and starting location.
- **Capacity Validation:** Prevents allocations that exceed vehicle carrying capacity.
- **Priority-Based Allocation:** Processes HIGH-priority orders before MEDIUM and LOW.
- **Distance Optimization:** Evaluates possible insertion positions across eligible vehicle routes and selects the position with the minimum additional distance.
- **Unassigned Order Tracking:** Returns orders that cannot be assigned to any available vehicle.
- **Input Validation:** Validates required fields, coordinate ranges, positive weights, capacities, and request structure.
- **Batch Upsert:** Uses batch retrieval of existing business IDs before saving orders and vehicles, supporting both inserts and updates.
- **API Documentation:** Provides interactive Swagger UI documentation.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java |
| Backend Framework | Spring Boot |
| API Style | REST |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Validation | Jakarta Bean Validation |
| API Documentation | Springdoc OpenAPI / Swagger UI |
| Boilerplate Reduction | Lombok |
| Testing | JUnit 5, Mockito |
| Build Tool | Maven |

## Project Structure

```text
src
└── main
    ├── java
    │   └── com.freightfox.dispatch_load_balancer
    │       ├── config
    │       ├── controller
    │       ├── dto
    │       │   ├── request
    │       │   └── response
    │       ├── entity
    │       │   └── enums
    │       ├── exception
    │       ├── repository
    │       ├── service
    │       │   └── model
    │       └── util
    └── resources
        └── application.yml

src
└── test
    └── java
        └── com.freightfox.dispatch_load_balancer
```

### Responsibilities

- `controller`: Exposes REST endpoints.
- `dto.request`: Request models and input validation.
- `dto.response`: API response models.
- `entity`: JPA entities and order-priority enum.
- `repository`: Database access.
- `service`: Order management, vehicle management, allocation, and dispatch-plan generation.
- `service.model`: Internal allocation result models.
- `util`: Haversine distance calculation.
- `exception`: Centralized exception handling.
- `config`: Application and OpenAPI configuration.

## Allocation Algorithm

The application uses a **priority-first, cheapest-insertion heuristic**.

### Step 1: Sort orders by priority

Orders are processed in the following order:

1. HIGH
2. MEDIUM
3. LOW

Orders with the same priority are sorted by their order ID to ensure deterministic processing.

### Step 2: Sort vehicles

Vehicles are sorted by vehicle ID to make allocation decisions deterministic when multiple options have the same cost.

### Step 3: Evaluate feasible vehicles

For each order:

- Check every vehicle.
- Verify that the remaining vehicle capacity can accommodate the order.
- Skip vehicles that do not have sufficient capacity.

### Step 4: Evaluate insertion positions

For each eligible vehicle, evaluate every possible position at which the new order can be inserted into its current route.

For an insertion between two existing route points:

```text
Previous ---- Next

Previous ---- New Order ---- Next
```

The additional distance is:

```text
Additional Distance =
    Distance(Previous, New Order)
  + Distance(New Order, Next)
  - Distance(Previous, Next)
```

For insertion at the end of a route:

```text
Additional Distance = Distance(Previous, New Order)
```

For an empty route, the additional distance is calculated from the vehicle's starting location to the new order.

### Step 5: Select the minimum-cost insertion

Among all feasible vehicle and insertion-position combinations, select the one with the smallest additional distance.

If multiple options have the same cost, use the deterministic vehicle ID and insertion-position tie-break rules.

### Step 6: Handle unassigned orders

If no vehicle has sufficient remaining capacity, the order is added to the unassigned-orders list.

### Important

This is a heuristic-based optimization approach. It aims to minimize the additional distance at each allocation step; it does not guarantee a globally optimal solution.

## Distance Calculation

The application uses the Haversine formula to calculate the great-circle distance between two geographical coordinates.

The formula is:

```text
a = sin²(Δφ / 2)
    + cos(φ1) × cos(φ2) × sin²(Δλ / 2)

c = 2 × atan2(√a, √(1 - a))

Distance = R × c
```

Where:

- `φ1`, `φ2` are the latitudes in radians.
- `λ1`, `λ2` are the longitudes in radians.
- `R` is the Earth's mean radius, approximately 6371.0088 km.

**Distance interpretation:**

- Distances are calculated between consecutive points in a vehicle's assigned route.
- The first leg starts at the vehicle's current location.
- The final leg ends at the last assigned delivery location.
- No return journey to the starting location is included.
- Distances represent straight-line geographical distances, not road distances.

## API Documentation

Base URL (local):

```text
http://localhost:8080
```

### 1. Upload Delivery Orders

**POST** `/api/dispatch/orders`

Request body:

```json
{
  "orders": [
    {
      "orderId": "ORD001",
      "latitude": 28.7041,
      "longitude": 77.1025,
      "address": "Karol Bagh, Delhi, India",
      "packageWeight": 25,
      "priority": "HIGH"
    },
    {
      "orderId": "ORD002",
      "latitude": 28.6139,
      "longitude": 77.2090,
      "address": "Connaught Place, Delhi, India",
      "packageWeight": 15,
      "priority": "MEDIUM"
    }
  ]
}
```

Success response:

```json
{
  "message": "Delivery orders accepted.",
  "status": "success"
}
```

### 2. Upload Vehicles

**POST** `/api/dispatch/vehicles`

Request body:

```json
{
  "vehicles": [
    {
      "vehicleId": "VEH001",
      "capacity": 100,
      "currentLatitude": 28.6139,
      "currentLongitude": 77.2090,
      "currentAddress": "Connaught Place, Delhi, India"
    },
    {
      "vehicleId": "VEH002",
      "capacity": 80,
      "currentLatitude": 28.5355,
      "currentLongitude": 77.3910,
      "currentAddress": "Noida, Uttar Pradesh, India"
    }
  ]
}
```

Success response:

```json
{
  "message": "Vehicle details accepted.",
  "status": "success"
}
```

### 3. Generate Dispatch Plan

**GET** `/api/dispatch/plan`

Example response structure:

```json
{
  "dispatchPlan": [
    {
      "vehicleId": "VEH001",
      "totalLoad": 95,
      "totalDistance": "14.44 km",
      "assignedOrders": [
        {
          "orderId": "ORD001",
          "latitude": 28.7041,
          "longitude": 77.1025,
          "address": "Karol Bagh, Delhi, India",
          "packageWeight": 25,
          "priority": "HIGH"
        }
      ]
    }
  ],
  "unassignedOrders": []
}
```

The response contains:

- Vehicle ID.
- Total assigned package weight.
- Total route distance.
- Assigned delivery orders.
- Orders that could not be assigned.

### 4. Swagger UI

Interactive API documentation:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

## Running Locally

### Prerequisites

- Java version configured in the Maven project.
- Maven (or the included Maven Wrapper).
- PostgreSQL.
- Git.

### 1. Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd dispatch-load-balancer
```

### 2. Configure the database

Create a PostgreSQL database and configure the connection in `src/main/resources/application.yml`.

Example configuration (adjust property names to match your application):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dispatch_load_balancer
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
```

Set the database environment variables before starting the application.

### 3. Build the application

Using Maven Wrapper:

```bash
./mvnw clean package
```

On Windows:

```bash
mvnw.cmd clean package
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

## Testing

Run the test suite:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

The test suite covers the distance-calculation utility and dispatch-plan generation/allocation behavior, including relevant edge cases.

Use Swagger UI or Postman to test the REST endpoints.

## Performance Benchmark

A larger dataset was generated to evaluate the allocation implementation.

| Metric | Result |
|---|---:|
| Delivery orders | 1,000 |
| Vehicles | 100 |
| Assigned orders | 1,000 |
| Unassigned orders | 0 |
| Duplicate assignments | 0 |
| Capacity violations | 0 |
| Maximum orders assigned to one vehicle | 31 |
| Total assigned load | 22,127.049 kg |
| Approximate aggregate route distance | 1,373.87 km |
| GET dispatch-plan response time | 549.36 ms |
| Response payload size | Approximately 173 KB |

**Benchmark note:** The response time was measured in the local development environment for the generated 1,000-order / 100-vehicle dataset. It is an observed result, not a production performance guarantee. Aggregate distance is the sum of per-vehicle distances rounded to two decimal places.

## Design Decisions and Limitations

### Design decisions

- Separate request and response DTOs from persistence entities.
- Use service classes to separate business logic from REST controllers.
- Use batch retrieval of existing business IDs before saving incoming records.
- Use deterministic sorting and tie-breaking for predictable allocation.
- Use immutable internal allocation result models.
- Centralize exception handling and request validation.
- Keep distance calculation in a dedicated utility class.

### Limitations

- The allocation strategy is heuristic-based and does not guarantee a globally optimal assignment.
- Haversine calculates straight-line distance and does not account for actual road networks, traffic, or travel time.
- The route does not include a return journey to the vehicle's starting location.
- Priority determines the order in which orders are considered for allocation; it does not necessarily determine the final stop sequence within a route.
- The current allocation approach evaluates vehicles and insertion positions for each order, so processing cost increases with the number of orders, vehicles, and route stops.

---

## Author

**Avinash Yennam**

Software Engineer | Java | Spring Boot | Microservices

