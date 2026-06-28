# Bank App

Spring Boot + Angular bank account demo. H2 in memory, no setup beyond JDK and Node.

## Run

Backend needs JDK 17+. Frontend needs Node 18+.

```bash
# backend
cd backend && mvn spring-boot:run

# frontend (separate terminal)
cd frontend && npm ci && npm start
```

UI: http://localhost:4200
API: http://localhost:8080
H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bankdb`, user `sa`, no password)

### Docker (backend only)

```bash
docker build -t bank-app ./backend
docker run --rm -p 8080:8080 bank-app
```

### Tests

```bash
cd backend && mvn test       # 24 tests
cd frontend && npm test
```

## Demo data

Seeded on startup:

- `alice.johnson` — accounts 1 (EUR), 2 (USD), 3 (SEK)
- `bob.smith` — accounts 4 (GBP), 5 (VND)

Each account has 5-10 pre-loaded transactions so the chart and history aren't empty.

## Poking the API

```bash
curl localhost:8080/api/users
curl localhost:8080/api/users/1/accounts

curl -X POST localhost:8080/api/accounts/1/credit \
  -H 'Content-Type: application/json' -d '{"amount": 50}'

# 422 INSUFFICIENT_FUNDS
curl -X POST localhost:8080/api/accounts/1/debit \
  -H 'Content-Type: application/json' -d '{"amount": 999999}'

curl -X POST localhost:8080/api/exchange \
  -H 'Content-Type: application/json' \
  -d '{"sourceAccountId":1,"targetAccountId":2,"amount":25}'

curl 'localhost:8080/api/accounts/1/transactions?page=0&size=5'
```

Errors all come back in the same shape:

```json
{"timestamp":"...","status":422,"errorCode":"INSUFFICIENT_FUNDS","message":"...","path":"/api/..."}
```

Status codes: 400 validation, 404 not found, 409 optimistic-lock conflict, 422 business-rule violation, 502 upstream failed.

## Layout

```
backend/    Spring Boot service
frontend/   Angular 19 SPA, NgRx state
```

Backend packages (hexagonal):

- `domain/` — entities, factories, specs. No Spring imports, plain JUnit tests.
- `application/` — services, handlers, ports. CQRS-ish command/query split.
- `adapter/in/web/` — controllers, DTOs, `GlobalExceptionHandler`.
- `adapter/out/persistence/` — JPA entities + repository adapters.
- `adapter/out/external/` — `httpstat.us` validator, Resilience4j circuit breaker.
- `config/` — `DataSeeder`, AOP, `RestTemplate` config.

Frontend:

- `core/services/api.service.ts` — one place for HTTP calls.
- `core/interceptors/error.interceptor.ts` — turns `HttpErrorResponse` into typed `ApiError`.
- `state/{user,account,transaction}/` — NgRx slices, entity adapters.
- `pages/` — `home`, `account-overview`, `transaction-detail`.

## Notes / gotchas

- `Account.balance` has no setter. Use `account.credit(x)` / `account.debit(x)`, or `Account.open(userId, currency)` to construct.
- `@Version` on `AccountJpaEntity` + `@Retryable(ObjectOptimisticLockingFailureException, maxAttempts=3, backoff=...)` on credit/debit/exchange. 409 only surfaces after retries exhaust.
- Exchange rejects `source == target` before touching the DB. Both legs share a `referenceId`.
- External validator is circuit-broken. When the breaker is open, debits go through and we log a warning. A real compliance system would want a different fallback.
- No auth. Anyone can hit any account by ID. See TODO.

## TODO

- Postgres + Flyway, Testcontainers integration tests
- AuthN/Z, ownership check on accounts
- MockMvc controller tests, Playwright E2E
- Split `account-overview-page` into smaller components
- Real exchange rates behind the existing circuit breaker
- Outbox table for domain events instead of in-process listener
- Springdoc / Swagger UI
