# Event Processor

Full-stack application that receives events sent by factory equipment, prevents duplicate processing and records an audit trail for quality events.

- **Backend:** Java 17, Spring Boot 4, Spring Data JPA, Flyway, H2 / PostgreSQL
- **Frontend:** Angular 20 (standalone components, signals, reactive forms)
- **Infrastructure:** Docker Compose (PostgreSQL + backend + frontend served by Nginx)

## Running the application

### Everything with one command (recommended)

Requirement: Docker.

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080/api/events |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Health check | http://localhost:8080/actuator/health |

Compose starts PostgreSQL, waits for it to be healthy, starts the backend (which applies the database migrations), waits for the backend health check and then starts the frontend.

If ports `4200` or `8080` are already in use, choose other host ports:

```bash
FRONTEND_PORT=4300 BACKEND_PORT=9090 docker compose up --build
```

To stop and remove all data: `docker compose down -v`.

### Backend and frontend separately (development)

**Backend** (requirement: Java 17+). Runs with an in-memory H2 database, no installation needed:

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The API is available at http://localhost:8080.

**Frontend** (requirement: Node.js 20+):

```bash
cd frontend
npm install
npm start
```

The application is available at http://localhost:4200. The Angular dev server proxies `/api` requests to `http://localhost:8080` (`proxy.conf.json`), so the backend must be running.

## Running the tests

```bash
cd backend
./mvnw test
```

13 tests: unit tests of the event service with Mockito, and integration tests of the HTTP API with MockMvc and H2.

```bash
cd frontend
npm test -- --watch=false
```

9 tests (Jasmine + Karma, requires Chrome): API service with `HttpTestingController`, error message mapping and the registration form.

## API

| Method | Path | Success | Errors |
|---|---|---|---|
| `POST` | `/api/events` | `201 Created` with `Location` header | `400` invalid payload, `409` event already processed |
| `GET` | `/api/events` | `200` events ordered by timestamp (newest first) | |
| `GET` | `/api/events/{eventId}` | `200` | `404` event not found |
| `GET` | `/api/audits` | `200` audit records (newest first) | |

Errors follow the [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) format:

```json
{
  "type": "about:blank",
  "title": "Duplicate event",
  "status": 409,
  "detail": "Event EVT-1001 has already been processed.",
  "instance": "/api/events"
}
```

Validation errors also include an `errors` object with the message of each invalid field.

### Example

```bash
curl -i -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"eventId":"EVT-1001","eventType":"QUALITY","description":"Inspection failed","timestamp":"2026-05-20T10:00:00"}'
```

Sending the same request again returns `409 Conflict`. Because the event type is `QUALITY`, `GET /api/audits` now contains a `QUALITY_EVENT_REGISTERED` record for `EVT-1001`.

## Architecture

```
event-processor/
├── backend/                     Spring Boot REST API
│   └── src/main/java/com/humberto/events/
│       ├── api/                 REST controllers and DTOs
│       ├── service/             business rules (EventService, AuditService)
│       │   └── handler/         one handler per event type (Strategy)
│       ├── domain/              JPA entities (Event, AuditRecord, EventType)
│       ├── repository/          Spring Data repositories
│       └── exception/           business exceptions and global error handler
├── frontend/                    Angular application
│   └── src/app/
│       ├── core/                models, API service and error mapping
│       ├── features/events/     events page (container), form and table
│       └── shared/components/   reusable UI components (alert)
└── compose.yaml                 PostgreSQL + backend + frontend
```

Flow of `POST /api/events`:

1. `EventController` validates the request (Bean Validation) and converts it to an `Event`.
2. `EventService.register` runs in a single transaction:
   - rejects the event if its ID has already been processed;
   - inserts the event; the primary key on `event_id` is the final guarantee against duplicates;
   - delegates to the `EventTypeHandler` of the event type: `PRODUCTION` is only registered, `QUALITY` also creates an `AuditRecord`.
3. `GlobalExceptionHandler` converts exceptions into Problem Details with the proper HTTP status.

In Docker, Nginx serves the Angular build and forwards `/api` to the backend, so the browser only talks to one origin and no CORS configuration is needed.

## Technical decisions

- **Duplicate protection enforced by the database.** `existsById` gives a fast, clear answer in the common case, but check-then-act is not safe under concurrency: two simultaneous requests with the same ID could both pass the check. The event ID is the primary key, `saveAndFlush` forces the insert inside the `try`, and a `DataIntegrityViolationException` is translated into the same `409 Conflict`.
- **`Event` implements `Persistable`.** The ID comes from the equipment, not from the database. Without `isNew()`, Spring Data would call `merge` for an entity with an ID and silently overwrite an existing event instead of failing.
- **Strategy pattern for event types.** Each type has its own `EventTypeHandler`, and `EventService` dispatches through an `EnumMap`. A new type means a new handler class, without changing the service. The application fails at startup if a type has no handler.
- **Event and audit in the same transaction.** A quality event is never stored without its audit record, and vice versa.
- **409 for duplicates.** The requirement is to prevent and report duplicate processing. An alternative for equipment that retries automatically would be an idempotent `200` returning the original event.
- **`timestamp` vs `receivedAt`.** The payload timestamp has no time zone, so it is stored as `LocalDateTime` (time reported by the equipment). The server also stores `receivedAt` as an `Instant` (UTC) to know when the event actually arrived.
- **DTOs separate from entities.** The API contract (`eventType`, `timestamp`) is independent of the database model (`type`, `occurred_at`).
- **Schema managed by Flyway**, with `ddl-auto=validate`. The same migration runs on H2 (local development and tests) and PostgreSQL (Docker), and Hibernate checks that the entities match the schema at startup.
- **Frontend split into container and presentational components.** `EventsPage` talks to the API and holds the state (signals). `EventForm` only validates and emits the request, and `EventList` only renders the table. The form is cleared only after the backend confirms the registration, so the user does not lose data on a `409`.

## Possible improvements

- Message queue (Kafka, SQS) between the equipment and the API, with idempotent consumers, for high event volumes.
- Pagination and filters (type, period) on `GET /api/events`.
- Authentication of the equipment (API key or mTLS) and of the frontend users.
- Time zone in the event timestamp (`OffsetDateTime`) to avoid ambiguity between factories.
- Asynchronous audit with the transactional outbox pattern, if auditing becomes slower or external.
- Correlation ID in logs and metrics per event type (Micrometer) for observability.
- Integration tests against PostgreSQL with Testcontainers, and end-to-end tests of the frontend.
- CI pipeline running both test suites on every push.
