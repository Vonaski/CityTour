# City Tour API

A REST API for managing city tours, guides, attractions, bookings, and tour statistics.

The application implements the complete tour lifecycle, including tour creation and editing, publishing, cancellation, seat booking, booking cancellation, capacity management, price calculation, validation, and statistics.

## Features

- Guide management
- Attraction management
- Tour creation and editing
- Tour lifecycle management
- Tour publishing and cancellation
- Tour stops management
- Guide schedule conflict detection
- Booking management
- Booking cancellation
- Seat availability and capacity validation
- Booking price calculation
- Tour summary
- Guide statistics
- Top attractions statistics
- Request validation
- Centralized exception handling
- PostgreSQL persistence
- Flyway database migrations
- Unit tests
- Integration test support

## Tech Stack

- Java 25
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Gradle
- JUnit 5
- Mockito
- MockMvc
- Lombok

## Architecture

The application follows a layered architecture:

    Controller
        |
        v
      Service
        |
        v
    Repository
        |
        v
    PostgreSQL

Main layers:

- `controller` — HTTP and REST API layer
- `service` — business logic and business rules
- `repository` — database access
- `entity` — JPA entities
- `dto` — API request and response models
- `exception` — application and business exceptions
- `mapper` — entity/DTO mapping

Business logic is implemented in the service layer, while controllers are responsible for HTTP-level concerns.

## Domain Model

The main domain entities are:

    Guide
      |
      | 1..N
      v
    Tour
      |
      | 1..N
      v
    TourStop
      |
      | N..1
      v
    Attraction

    Tour
      |
      | 1..N
      v
    Booking

### Guide

A guide contains:

- ID
- Full name
- Phone number
- Languages
- Years of experience
- Active status

### Attraction

An attraction contains:

- ID
- Name
- Address
- Latitude
- Longitude
- Category
- Entry fee

### Tour

A tour contains:

- Title
- Guide
- Start time
- End time
- Maximum seats
- Price per seat
- Status
- Tour stops
- Booked seats

### Tour Stop

A tour stop contains:

- Attraction
- Visit order
- Stay duration

### Booking

A booking contains:

- Tour
- Customer name
- Customer phone
- Number of seats
- Total price
- Status
- Creation timestamp

## Tour Lifecycle

A tour can have the following statuses:

    DRAFT
      |
      | publish
      v
    PUBLISHED
      |
      | cancel
      v
    CANCELLED

Only draft tours can be updated.

Bookings are allowed only for published tours.

Bookings are not allowed after the tour start time.

## Business Rules

### Tour Creation

When creating a tour, the application validates:

- The guide exists
- All attractions exist
- The time range is valid
- `maxSeats >= 1`
- `pricePerSeat > 0`
- `stayMinutes >= 5`
- Attractions are not duplicated within the same tour
- The guide does not have an overlapping tour

### Visit Order

Tour stop `visitOrder` values must start from `1` and be sequential.

Valid:

    1
    2
    3

Invalid:

    1
    2
    4

Invalid:

    2
    3

Invalid:

    1
    3

The visit order is validated when publishing a tour.

### Tour Duration

The total duration of all tour stops cannot exceed the duration of the tour.

For example:

    Tour:
    10:00 - 12:00
    Duration: 120 minutes

    Stops:
    60 + 60 = 120 minutes

    Result: valid

But:

    Tour:
    10:00 - 11:00
    Duration: 60 minutes

    Stops:
    40 + 40 = 80 minutes

    Result: invalid

### Guide Overlap

A guide cannot have two tours that overlap in time.

Example:

    Tour A:
    10:00 - 13:00

    Tour B:
    12:59 - 16:00

    Result: overlapping tours

Sequential tours are allowed:

    Tour A:
    10:00 - 13:00

    Tour B:
    13:00 - 16:00

    Result: valid

### Duplicate Attractions

An attraction cannot be used more than once within the same tour.

Invalid:

    Registan Square
    Gur-e-Amir Mausoleum
    Registan Square

The API returns:

    TOUR_DUPLICATE_ATTRACTION

### Booking Capacity

The number of confirmed seats cannot exceed the tour capacity.

Example:

    maxSeats = 5
    confirmedSeats = 3
    requestedSeats = 2

    Result: booking allowed

But:

    maxSeats = 5
    confirmedSeats = 3
    requestedSeats = 3

    Result: NOT_ENOUGH_SEATS

When a booking is cancelled, its seats become available again.

### Booking After Tour Start

A booking cannot be created after the tour start time.

The API returns:

    TOUR_ALREADY_STARTED

### Booking Price

The booking price is calculated using the tour price per seat and the applicable attraction entry fees according to the project's business formula.

For example, a tour with:

    pricePerSeat = 100
    seats = 2

and attraction entry fees:

    50,000
    30,000

will have its total booking price calculated by the booking service according to the configured business rules.

## Error Handling

The API uses a consistent error response format.

Example:

    {
      "code": "NOT_ENOUGH_SEATS",
      "errors": null,
      "message": "Not enough seats available",
      "path": "/api/tours/21/bookings",
      "status": 409,
      "timestamp": "2026-08-10T06:34:53.775925400Z"
    }

Main HTTP statuses:

| Status | Description |
|--------|-------------|
| 200 | Successful request |
| 201 | Resource created |
| 204 | Resource deleted |
| 400 | Invalid request or validation error |
| 404 | Resource not found |
| 409 | Business rule violation |
| 500 | Unexpected server error |

Examples of business error codes:

- `GUIDE_NOT_FOUND`
- `ATTRACTION_NOT_FOUND`
- `TOUR_NOT_FOUND`
- `TOUR_NOT_PUBLISHED`
- `TOUR_ALREADY_STARTED`
- `NOT_ENOUGH_SEATS`
- `GUIDE_HAS_OVERLAPPING_TOUR`
- `TOUR_DUPLICATE_ATTRACTION`
- `TOUR_INVALID_VISIT_ORDER`
- `TOUR_DURATION_TOO_SHORT`
- `TOUR_CANNOT_BE_UPDATED`
- `ATTRACTION_USED_BY_ACTIVE_TOUR`

## API

Base URL:

    http://localhost:8080

### Tours

Create a tour:

    POST /api/tours

Get a tour:

    GET /api/tours/{id}

Update a draft tour:

    PUT /api/tours/{id}

Delete a tour:

    DELETE /api/tours/{id}

Publish a tour:

    POST /api/tours/{id}/publish

Cancel a tour:

    POST /api/tours/{id}/cancel

Get tour summary:

    GET /api/tours/{id}/summary

### Bookings

Create a booking:

    POST /api/tours/{tourId}/bookings

Get a booking:

    GET /api/bookings/{id}

Cancel a booking:

    DELETE /api/bookings/{id}

Get bookings for a tour:

    GET /api/tours/{tourId}/bookings

Filter bookings by status:

    GET /api/tours/{tourId}/bookings?status=CONFIRMED

    GET /api/tours/{tourId}/bookings?status=CANCELLED

### Attractions

Get an attraction:

    GET /api/attractions/{id}

Delete an attraction:

    DELETE /api/attractions/{id}

### Statistics

Get guide statistics:

    GET /api/statistics/guides?from=2026-08-01&to=2026-08-31

Get top attractions:

    GET /api/statistics/attractions/top?limit=5&from=2026-08-01&to=2026-08-31

## API Examples

### Create a Tour

    curl -i -X POST "http://localhost:8080/api/tours" \
      -H "Content-Type: application/json" \
      -d '{
        "title": "Samarkand Heritage Tour",
        "guideId": 1,
        "startTime": "2026-08-15T10:00:00",
        "endTime": "2026-08-15T13:00:00",
        "maxSeats": 10,
        "pricePerSeat": 150.00,
        "stops": [
          {
            "attractionId": 1,
            "visitOrder": 1,
            "stayMinutes": 60
          },
          {
            "attractionId": 2,
            "visitOrder": 2,
            "stayMinutes": 45
          },
          {
            "attractionId": 3,
            "visitOrder": 3,
            "stayMinutes": 60
          }
        ]
      }'

Example response:

    {
      "id": 1,
      "title": "Samarkand Heritage Tour",
      "status": "DRAFT",
      "maxSeats": 10,
      "bookedSeats": 0,
      "freeSeats": 10,
      "pricePerSeat": 150.00
    }

### Publish a Tour

    curl -i -X POST \
      "http://localhost:8080/api/tours/1/publish"

### Create a Booking

    curl -i -X POST \
      "http://localhost:8080/api/tours/1/bookings" \
      -H "Content-Type: application/json" \
      -d '{
        "customerName": "Test Customer",
        "customerPhone": "+998901234567",
        "seats": 2
      }'

### Cancel a Booking

    curl -i -X DELETE \
      "http://localhost:8080/api/bookings/1"

### Get Tour Summary

    curl -i \
      "http://localhost:8080/api/tours/1/summary"

Example response:

    {
      "freeSeats": 1,
      "bookedSeats": 9,
      "occupancyRate": 90.0,
      "totalRevenue": 421350.00,
      "totalStayMinutes": 165,
      "stopsCount": 3
    }

## Statistics

### Guide Statistics

Guide statistics include:

- Number of tours
- Total tour hours
- Seats sold
- Total revenue
- Occupancy rate
- Top attraction category

Example:

    {
      "guideId": 1,
      "guideName": "Updated Test Guide",
      "toursCount": 1,
      "totalHours": 3.0,
      "seatsSold": 9,
      "totalRevenue": 421350.00,
      "occupancyRate": 90.0,
      "topCategory": "MONUMENT"
    }

### Top Attractions

Attraction statistics include:

- Attraction ID
- Attraction name
- Category
- Number of tours
- Visitor count

Example:

    {
      "attractionId": 1,
      "name": "Registan Square",
      "category": "MONUMENT",
      "tourCount": 2,
      "visitorCount": 11
    }

## Database

PostgreSQL is used as the primary database.

Database schema changes are managed with Flyway.

Migration files are located in:

    src/main/resources/db/migration/

Flyway applies migrations automatically during application startup.

## Configuration

Application configuration is located in:

    src/main/resources/application.properties

Example:

    spring.datasource.url=jdbc:postgresql://localhost:5432/city_tour
    spring.datasource.username=postgres
    spring.datasource.password=postgres

    spring.jpa.hibernate.ddl-auto=validate

Production credentials should not be committed to the repository.

Environment variables should be used for sensitive configuration in production environments.

## Running the Application

### Requirements

- JDK 25
- PostgreSQL
- Git

### Clone the Repository

    git clone <repository-url>
    cd city-tour

### Create the Database

Create a PostgreSQL database:

    CREATE DATABASE city_tour;

### Run the Application

Linux/macOS:

    ./gradlew bootRun

Windows:

    gradlew.bat bootRun

The API will be available at:

    http://localhost:8080

## Running Tests

Run all tests:

    ./gradlew test

Windows:

    gradlew.bat test

Run a specific test class:

    ./gradlew test --tests "com.iksanov.citytour.booking.service.BookingServiceTest"

Test reports are generated in:

    build/reports/tests/test/

## Testing Strategy

The project uses unit tests and integration testing.

### Unit Tests

Business logic is tested independently using JUnit 5 and Mockito.

Tested areas include:

- Booking creation
- Seat capacity validation
- Booking cancellation
- Total price calculation
- Tour lifecycle
- Guide overlap validation
- Visit order validation
- Tour duration validation
- Business exception handling
- Input validation

### Integration Tests

HTTP-level integration tests can be implemented using Spring Boot and MockMvc.

The goal is to verify the complete request flow:

    HTTP Request
        |
        v
    Controller
        |
        v
    Service
        |
        v
    Repository
        |
        v
    Database
        |
        v
    HTTP Response

## Project Structure

    src/
    ├── main/
    │   ├── java/
    │   │   └── com/iksanov/citytour/
    │   │       ├── attraction/
    │   │       ├── booking/
    │   │       ├── common/
    │   │       ├── guide/
    │   │       ├── statistics/
    │   │       └── tour/
    │   │
    │   └── resources/
    │       ├── db/
    │       │   └── migration/
    │       └── application.properties
    │
    └── test/
        └── java/
            └── com/iksanov/citytour/

## Development Principles

The project follows these principles:

- Business logic is kept in the service layer.
- Controllers are responsible for HTTP concerns.
- Database access is isolated behind repositories.
- DTOs are used as API contracts.
- Business rules are represented explicitly in the service layer.
- Business errors are represented by dedicated exceptions.
- API errors use a consistent response format.
- Database schema changes are managed through Flyway.
- Business-critical logic is covered by automated tests.
- Validation is performed at the API boundary and in the service layer where required.

## Future Improvements

Potential improvements for the project include:

- OpenAPI / Swagger documentation
- Docker Compose for local development
- Testcontainers for database integration tests
- CI pipeline
- API authentication and authorization
- Pagination for collection endpoints
- Database query optimization
- Structured application logging
- Metrics and monitoring

## License

This project was developed for educational and portfolio purposes.