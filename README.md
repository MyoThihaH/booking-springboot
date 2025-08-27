# Booking System

A class booking system with credit-based packages.

## Quick Start

```bash
./mvnw spring-boot:run
```

## Supported Countries
- Singapore (SG)
- Myanmar (MM)  
- Thailand (TH)

## How It Works
1. Users register and purchase country-specific credit packages
2. Credits are used to book fitness/wellness classes
3. Classes have waitlists when full
4. Cancellations 4+ hours before class start refund credits

## API Documentation
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Requirements
- Java 17+
- PostgreSQL
- Redis (for booking locks)