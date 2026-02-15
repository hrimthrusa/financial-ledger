# Financial Ledger Microservice

A simplified financial ledger system implementing double-entry bookkeeping.

## Tech Stack

- Java 17+
- Spring Boot 3
- PostgreSQL
- Spring Data JPA
- Flyway
- Testcontainers
- JUnit 5

---

## Overview

This service implements a basic financial ledger following the core rule of double-entry bookkeeping:

> Total debits must equal total credits for every transaction.

Account balances are **derived from transaction entries**, not stored as state.

---

## Architecture

Layered architecture with clear separation of concerns:

```
ledger
 ├─ api           # Controllers, DTOs, error handling
 ├─ application   # Use cases / services
 ├─ domain        # Business rules
 └─ persistence   # JPA entities & repositories
```

- Business rules isolated in the domain layer (`TransactionRules`, `BalanceRules`)
- Application layer orchestrates persistence and domain logic
- Integration tests run against real PostgreSQL (Testcontainers)

---

## API

### Accounts

- `POST /api/accounts`
- `GET /api/accounts`
- `GET /api/accounts/{id}`

### Transactions

- `POST /api/transactions`
- `GET /api/transactions/{id}`
- `GET /api/accounts/{id}/transactions`

---

## Validation Rules

- Account names must be unique and non-empty
- Transactions must have at least 2 entries
- At least one DEBIT and one CREDIT per transaction
- Total debits must equal total credits
- Amounts must be positive
- Referenced accounts must exist

---

## Running Locally

### Start PostgreSQL

```
docker-compose up -d
```

### Run application

```
./gradlew bootRun
```

Application runs on:

```
http://localhost:8080
```
---

## Running Tests

```
./gradlew test
```

Integration tests use Testcontainers and do not require local PostgreSQL.

---

## Notes

- Database schema managed via Flyway
- Balance calculated dynamically from transaction entries
- Includes unit tests for domain rules and integration tests for API

---

## Assumptions & Limitations

- Single currency system
- No pagination implemented
- No authentication/authorization
- No optimistic locking
- No concurrency conflict handling