# Instructions for challengers
This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

This is a payment gateway application, an API based application with two main features:
1. **Process payments:** Validate payment requests, call a bank simulator and return the status of the resulting payment.
2. **Retrieve payment details:** Find and return the payment details of any stored, previous valid payments (non-rejected payments).

## Requirements
- JDK 17
- Docker

## Running the Application
```bash
#1. Start the bank simulator:
docker-compose up

# 2. Run the gateway application:
./gradlew bootRun

# 3. Access Swagger UI:
http://localhost:8090/swagger-ui/index.html

# 4. Run the test suite
./gradlew test
```
- API: `http://localhost:8090/api/v1/payments`
- Swagger UI: `http://localhost:8090/swagger-ui/index.html`
- Actuator health: `http://localhost:8090/actuator/health`

## Assumptions
1. Spec's Declined scenario card fails Luhn check (invalid card number) so it shouldn't be sent to the simulator.
2. Only store payments in the repository that are valid (Authorized/Declined). Rejected requests do not get stored.
3. Never store full card numbers
4. Keep the gateway logic simple (no over-engineering), i.e. no Authentication, Real database, Idempotency Tokens, CQRS etc...
5. Amount must be greater than zero (note: ask PO if zero-value authorisations should be supported).
6. Spec "Integer", can be interpreted as int64 (long).
7. Cards with expiry date of the current month/year are treated as expired.
8. Bank errors can be simplified into a generic ‘bank unavailable’ response, but in production I’d separate retryable failures (e.g. 5xx, timeouts) from non-retryable ones (e.g. auth errors).
9. If Bank unavailable, payments should not should be stored. Even if they were to be non-rejected.
10. There is no "maximum" on the expiry year for a card.

## Functional Requirements
The functional requirements to be covered from the spec:
- A merchant should be able to process a payment through the payment gateway and receive one of the following types of response:
    - **Authorized** - the payment was authorized by the call to the acquiring bank
    - **Declined** - the payment was declined by the call to the acquiring bank
    - **Rejected** - no payment could be created as invalid information was supplied to the payment gateway and therefore it has rejected the request without calling the acquiring bank
-  A merchant should be able to retrieve the details of a previously made (Authorised or Declined) payment.

## API Endpoints
### POST /api/v1/payments
Process a payment request.

**Request Body:**
```json
{
  "card_number": "2222405343248877",
  "expiry_month": 4,
  "expiry_year": 2030,
  "currency": "GBP",
  "amount": 100,
  "cvv": "123"
}
```

**Response Codes:**
- `200 OK` — Payment processed (`Authorized` or `Declined`)
- `400 Bad Request` — Validation failed; returns `Rejected` status with error list
- `400 Bad Request` — Malformed request body or invalid field types
- `502 Bad Gateway` — Acquiring bank returned an unexpected response
- `503 Service Unavailable` — Acquiring bank is unreachable
- `500 Internal Server Error` — Unexpected internal error

### GET /api/v1/payments/{id}
Retrieve a previously processed payment by ID.

**Response Codes:**
- `200 OK` — Payment found
- `400 Bad Request` — `{id}` is not a valid UUID
- `404 Not Found` — No payment exists with the given ID

> Only `Authorized` and `Declined` payments are stored. `Rejected` requests are never persisted.

## Supported Workflows
**Process new payments**
1. Merchant sends a payment request to our gateway
2. The request has each field validated.
3. If any fields invalid -> Return Rejected Payment Response (don't even call the bank or repository)
4. If all fields valid -> Call the bank simulator
5. Store payment details (masking card and without cvv), with its resulting status (declined or authorised) in an in-memory database. 
6. Return response to the merchant.

**Retrieve a payment by ID**
1. Merchant sends a GET request with a payment UUID to the gateway
2. If `{id}` is not a valid UUID → return `400 Bad Request`
3. The gateway looks up the payment in the repository
4. If not found → return `404 Not Found`
5. If found → return the masked payment details

## Features

- ✅ Field-level validation (card number with Luhn check, expiry date, currency, amount, CVV)
- ✅ Acquiring bank integration with 10s connect/read timeouts
- ✅ Card masking — last 4 digits only; CVV never stored
- ✅ Thread-safe in-memory storage (`ConcurrentHashMap`)
- ✅ Comprehensive automated testing
- ✅ Structured logging with contextual fields and rolling file policy
- ✅ Health check endpoint (`/actuator/health`) with custom `BankHealthIndicator`
- ✅ Swagger / OpenAPI docs at `http://localhost:8090/swagger-ui/index.html`


## Technical Discussion
### Solution Structure
Below is a directory tree of the solution. The test package structure closely mirrors main, so it has been collapsed to top-level packages only for readability.
```
src/
├── main/
│   ├── resources/
│   └── java/
│       └── com/checkout/payment/gateway/
│           ├── api/
│           ├── bank/
│           ├── config/
│           └── domain/
└── test/
    └── java/
        └── com/checkout/payment/gateway/
            ├── api/
            ├── bank/
            ├── domain/
            └── integration/
```
The main codebase is organised into a 3-tier structure: api, domain, and bank. 
Heres a breakdown of each tier:
1. **API layer** (api/)
    - The entry point for all incoming HTTP requests from merchants. It contains the REST controller, request/response DTOs and a global exception handler.
    - Main responsibility is to interact with external clients, handling HTTP requests, delegating to service and sending responses.
2. **Domain Layer** (domain/)
    - The core layer, performing the business logic of our gateway, keeping it independent of both the API and bank layers.
    - It contains the payment model, repository, service and validation logic.
    - Contains mappers translating internal domain models into API responses, keeping the API contract decoupled from the business logic.
    - The service orchestrates the payment processing flow. It delegates validation through a chain of field validators before forwarding to the acquiring bank. 
    - Interacts with an in-memory database to store processed payments or retrieve them by ID.
3. **Acquiring Bank Layer** (bank/)
    - Encapsulates all communication with the external bank simulator.
    - Includes request/response DTOS and mappers to adhere to the bank's API contract.
    - Isolates bank related exceptions, ensuring failures are handled cleanly without leaking concerns into the other layers.
    - Contains a health indicator to allow monitoring the availability of the acquiring bank via Spring Actuator.

---

### Design Decisions
**Architecture:** 
Simple layered pattern (Controller -> Service -> Validation -> BankClient -> Repository)
KISS. Easier to understand, maintain and test. Opted against CQRS to avoid over-engineering.

**Validation:** 
Field-level validators following the Open/Closed Principle.
Allows extension without modifying existing validation logic.

**Testing:** 
Primarily unit tests for core logic, with some integration testing for component interaction.
Follows the testing pyramid (albeit with missing levels).

**Mapping:**
Manual mapping to keep transformations explicit and avoid hidden behaviour from code generation tools (e.g. MapStruct).

**Data Storage:**
Replaced in-memory storage backed by a HashMap, to a ConcurrentHashMap instead, to ensure thread-safe read and writes without needing explicit synchronisation. Since multiple requests could arrive simultaneously, a plain HashMap is not thread-safe and could produce corrupted state. ConcurrentHashMap handles this with fine-grained internal locking, providing safe concurrent operations with minimal performance overhead.

### Reliability
The system is designed to behave predictably in the presence of invalid input and downstream failures.
- **Input validation** ensures only well-formed, valid requests are processed, preventing invalid state from entering the system.
- **Controlled error handling** via a global exception handler ensuring consistent and safe responses to clients.
- **Failure isolation**: issues communicating with the acquiring bank are contained and returned as controlled API errors rather than propagating raw failures.

*Limitation:* The current use of an in-memory repository means data is lost on restart, so durability and recovery guarantees are not met. A production system would require persistent, replicated storage.

### Scalability
The system is designed with horizontal scaling in mind.

- **Stateless application layer** would allow multiple instances to handle requests independently behind a load balancer.
- **Decoupled components** (validation, service, bank client) enable scaling.

*Limitation:* The current in-memory data store does not scale beyond a single instance and would introduce consistency issues in a distributed setup. A shared, distributed datastore would be required for true horizontal scalability.

### Maintainability
The codebase is structured to be easy to understand, extend, and modify over time.

- **Clear separation of concerns** (API, domain, bank layers) keeps responsibilities well-defined and reduces coupling.
- **Modular validation design** Allows new rules to be added without modifying existing logic.
- **Explicit data mapping** avoids hidden behaviour and keeps transformations transparent.
- **Comprehensive unit testing** gives confidence when making changes and helps prevent regressions.

*Trade-off:* Mapping is implemented manually to keep transformations explicit and easy to follow, avoiding hidden behaviour from external dependencies. This works well at the current scale, but as the number of DTOs and mappings grows it may lead to repetitive boilerplate and increased maintenance overhead, at which point a tool like MapStruct would be considered to reduce duplication.

### Observability
Observability is implemented by using: structured logging and health monitoring.

Logging — SLF4J with Logback is used across all layers, with log statements at appropriate levels (DEBUG, INFO, WARN, ERROR). Each log line includes added contextual fields such as paymentId, status, amount, and masked lastFour, enabling tracing of individual payment journeys. Logs roll to file (logs/payment-gateway.log) with 10MB size cap, gzip compression, and 30-day retention.

Health Checks — Spring Actuator exposes /actuator/health with a custom BankHealthIndicator that actively probes the acquiring bank and reports its reachability, enabling infrastructure tooling to detect downstream degradation early.

*Limitation:* The current setup lacks distributed tracing, correlation ID propagation, and metrics.

## Testing
My testing strategy follows the testing pyramid, with a strong focus on unit testing core business logic and a smaller number of integration tests.

### Unit Tests
Unit tests cover the smallest pieces of logic in isolation directly, including:
- **Validators** – ensure invalid inputs are correctly rejected.
- **Service layer** – verifies the payment processing flow, with dependencies (bank client, repository) mocked.
- **Mappers** – confirm correct transformation between domain and API models.
This provides fast feedback and high confidence in the core logic.

### Integration Tests
Integration tests validate that components work together correctly:
- Exercised the API layer end-to-end via HTTP.
- Verified request validation, service orchestration, and response formatting together.
- Used the bank simulator to test real interactions with the external dependency.

#### Trade-off
- The test suite prioritises coverage of core business logic over exhaustive end-to-end scenarios, keeping tests fast and maintainable while still validating critical flows. I felt this distribution reflected the testing pyramid better.

## Non-Functional Requirements
Although the spec doesnt mention any NFRs, below are a set of potential non-functional requirements with business cases for inclusion for the full solution.
- Correctness & Consistency – Payment state must be accurate and consistent. Strong consistency is required; conflicting or invalid payment states are not acceptable in financial systems.
- Idempotency & Data Integrity – The system must prevent duplicate charges caused by retries. Requests should be safely repeatable (e.g. via idempotency keys) and enforce valid state transitions.
- Durability – Once a payment is acknowledged, it must not be lost. A production system requires persistent, replicated storage. The current in-memory store is a known limitation.
- Availability – The gateway should maintain high availability (target ≥ 99.99%) and degrade gracefully when dependencies (e.g. the acquiring bank) are unavailable.
- Latency & Throughput – Payment requests must complete quickly (low latency) while supporting high request volumes with predictable performance.
- Scalability – The system must scale horizontally to handle variable and potentially very high transaction volumes.
- Fault Tolerance – The system should handle partial failures (e.g. network issues, bank outages) using timeouts, retries, and controlled failure responses.
- Observability – The system must provide logs, metrics, and health checks to enable monitoring, alerting, and debugging in production.
- Security & Compliance – Sensitive data must be protected (TLS, encryption at rest, PCI-DSS principles). Full card details and CVV must never be stored.
- Evolvability – The system should support safe iteration over time (e.g. API versioning, schema evolution) without breaking existing clients.

## Future improvements
- **Persistence:** Replace the in-memory repository with a persistent datastore (e.g. PostgreSQL), using a unique payment ID and indexed lookups to support retrieval and ensure data durability across restarts and instances.
- **Idempotency:** Introduce idempotency keys on payment requests, backed by a datastore with uniqueness constraints to guarantee that duplicate requests do not result in multiple charges.
- **Resilience:** Add retry logic (with exponential backoff) and a circuit breaker around the bank client to handle transient failures (e.g. timeouts, 5xx responses) and fail fast when the bank is unavailable.
- **Security:** Implement merchant authentication (e.g. API keys or OAuth client credentials) and move towards tokenisation or encryption of card data to reduce PCI scope and limit exposure of sensitive information. Put actuator behind auth.
- **Observability:** Extend health checks to include downstream dependencies (e.g. bank connectivity), and add structured logging and metrics (e.g. request latency, error rates) for monitoring and alerting.
- **Testing:** Expand integration tests to include failure scenarios (e.g. bank unavailability). Add end-to-end tests to validate the full request lifecycle, API contract tests to ensure interface consistency to verify overall behaviour in a production-like environment.
- **Caching:** Can be introduced on the retrieve payment by id endpoint. Since retrieved payments are immutable (their status never changes once stored), they are ideal cache candidates. We can introduce a simple read-through cache with a reasonable TTL to reduce repository lookups and improve latency.
- **Traceability:** Accept an X-Correlation-Id header from the client (or generate a UUID if absent). Then, we push it into the SLF4J MDC so it appears on every log line within that request's context, and finally echo it back in the response. This allows individual payment journeys to be traced across logs and gives merchants a reference ID for support queries.
- **CI/CD Pipeline:** Introduce an automated pipeline (e.g. via GitHub Actions) to enforce code quality and safety on every push. This would include compiling the project, running the full test suite, some static analysis (e.g. Checkstyle), and building a Docker image for deployment. A passing pipeline would be required before merging, preventing regressions and ensuring the gateway is always in a releasable state.
- **Upgrade Vulnerable Dependencies:** Several dependencies have CVEs and thus need upgrading to maintain the secureness of the gateway.
- **Documentation:** Further javadoc to all appropriate classes and methods.

## Configuration
All application configuration is managed via `application.properties`.

**Bank Simulator URL** — externalised to allow targeting different environments without needing to change code:
```properties
bank.simulator.base.url=http://localhost:8080
```

**Server Port** — the gateway runs on port `8090` by default:
```properties
server.port=8090
```

**Actuator & Health** — health endpoint exposed with full detail; noisy default checks are disabled:
```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
management.health.diskspace.enabled=false
management.health.ping.enabled=false
```

**Logging** — rolling file with 10MB cap, gzip compression, and 30-day retention:
```properties
logging.file.name=logs/payment-gateway.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=30
logging.logback.rollingpolicy.file-name-pattern=logs/payment-gateway.%d{yyyy-MM-dd}.%i.log.gz
```
