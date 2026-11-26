# Distributed Movie Ticket Booking System

A BookMyShow-style booking platform built as a **modular Spring Boot monolith** with a focus on
concurrency safety: many users can try to book the same seat at the same moment, and only one succeeds.

> Status: Week 1 done (auth, catalog, shows, seat availability). Booking, Redis locking, RabbitMQ and AWS deployment follow.

## Tech stack

Java 17, Spring Boot 4, Spring Security (JWT, BCrypt), Spring Data JPA / Hibernate, Flyway, MySQL 8,
Docker Compose. Planned: Redis, RabbitMQ, Nginx, AWS EC2/RDS/S3, Prometheus/Grafana, JMeter.

## Architecture

One Spring Boot application split into modules that talk only through each other's services:

    auth  user  movie  theatre  screen  seat  show  booking  payment  notification

Stateless JWT auth, so the same app can later run on several EC2 instances behind Nginx.

## Run locally

Requirements: JDK 17+, Docker, docker-compose.

    cp .env.example .env              # then edit the passwords
    docker-compose up -d              # MySQL
    set -a && source .env && set +a
    cd backend && ./mvnw spring-boot:run

Health check: GET http://localhost:8080/api/health

Default admin (development only): admin@moviebooking.com, configured in application.yaml.

## API overview

    POST /api/auth/register, /api/auth/login
    GET  /api/movies  (filters: name, genre, language, city; pagination)   ADMIN: POST, PUT, DELETE
    GET  /api/theatres?city=          ADMIN: POST theatre, POST /api/theatres/{id}/screens
    GET  /api/screens/{id}/seats      ADMIN: POST /api/screens/{id}/seats/generate
    GET  /api/shows  (filters: movieId, theatreId, city, date)   GET /api/shows/{id}/seats   ADMIN: POST /api/shows

## Design decisions worth reading

- **Double-booking protection in the database.** booking_seats has a unique key on (show_id, seat_id, active).
  active is 1 while a seat is held and NULL once released; MySQL allows many NULLs in a unique index, so released
  rows never conflict but two live holds on one seat are impossible, even if Redis fails.
- **Show overlap prevention.** Creating a show locks the screen row (SELECT ... FOR UPDATE) under READ_COMMITTED,
  then checks for time overlap. Two admins creating clashing shows at the same instant: one wins, one gets 409.
- **Flyway owns the schema**, Hibernate only validates it (ddl-auto: validate).
- **Consistent error contract.** Every failure returns the same JSON shape; no stack traces or SQL leak out.

## Testing

    cd backend && ./mvnw test -Dtest='ShowServiceTest,AuthServiceTest,JwtServiceTest'
    ./infra/scripts/smoke-test.sh     # end-to-end flow against a running backend

## Roadmap

Week 2: booking flow, transactions and isolation levels, Redis seat locks, idempotency, mock payments.
Week 3: RabbitMQ with retry and DLQ, Docker images, AWS (RDS, EC2, S3), React frontend.
Week 4: second EC2 + Nginx load balancing, Prometheus/Grafana, JMeter (500 users on one seat), failure testing.
