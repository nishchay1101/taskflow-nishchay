# TaskFlow

## Overview

TaskFlow is a REST API for team-based task management. Users can create projects, assign tasks, track status, and filter by assignee or priority. Built as a take-home assignment for a Backend Engineer role at Zomato.

The focus is on correctness, clean API design, and production patterns — not features.

## Tech Stack

| Component | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Auth | Spring Security + JWT (jjwt, HS256) |
| Password hashing | BCrypt (cost 12) |
| Build tool | Gradle (Kotlin DSL) |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5 + Testcontainers |
| Logging | SLF4J + Logback (JSON in Docker, plain text locally) |

---

## Running Locally

### Prerequisites
- Docker and Docker Compose installed
- Nothing running on port 8080

---

### ⚡ Option A — Pre-built image (fastest, ~30 seconds)

No compilation. Docker pulls the pre-built image directly from Docker Hub.

```bash
# 1. Clone the repo
git clone https://github.com/nishchay1101/taskflow-nishchay.git
cd taskflow-nishchay

# 2. Copy env file
cp .env.example .env

# 3. Pull and start
docker compose up -d
```

The `docker-compose.yml` is already configured to use `gotsphinx/taskflow-api:latest` — no changes needed.

---

### 🔨 Option B — Build from source (~3-5 min)

If you want to compile the code yourself:

```bash
# 1. Clone and copy env
git clone https://github.com/nishchay1101/taskflow-nishchay.git
cd taskflow-nishchay
cp .env.example .env

# 2. In docker-compose.yml, comment out the image: line and uncomment build:
#    image: nishchay1101/taskflow-api:latest  ← comment this out
#    build: ./backend                          ← uncomment this

# 3. Build and start
docker compose up --build
```

---

That's it. Flyway migrations run automatically on startup. The API is available at `http://localhost:8080`.

### Verify it's working

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"arjun@example.com","password":"password123"}' | jq .
```

You should receive a JWT token in the response.

### Running tests

```bash
cd backend
./gradlew clean test
```

Tests use Testcontainers (real PostgreSQL, not H2). Covers auth flows, project CRUD authorization, task filtering, pagination, stats, and delete permissions. Docker must be running. No other setup needed.

---

## Seed Data

The seed migration (`V4__seed_data.sql`) creates 3 users, 3 projects, and 6 tasks with varied statuses, priorities, and assignees so all features are immediately demonstrable without creating data manually.

### Users

All passwords are `password123` (BCrypt cost 12).

| Name | Email | Role across projects |
|---|---|---|
| Arjun Sharma | arjun@example.com | Owner of Project 1 and 3, assignee in Project 2 |
| Priya Mehta | priya@example.com | Owner of Project 2, assignee in Project 1 |
| Rohan Verma | rohan@example.com | Assignee in Project 1 and 3 |

### Projects

| Project | Owner | Description |
|---|---|---|
| Zomato Payments Revamp | Arjun Sharma | Payment gateway integration |
| Rider App Redesign | Priya Mehta | Rider-facing mobile app UX |
| Internal Analytics Dashboard | Arjun Sharma | Ops delivery metrics dashboard |

### Tasks

| Title | Project | Status | Priority | Assignee |
|---|---|---|---|---|
| Integrate Razorpay webhook handler | Payments Revamp | IN_PROGRESS | HIGH | Priya Mehta |
| Write unit tests for payment service | Payments Revamp | TODO | MEDIUM | Rohan Verma |
| Deprecate old payment gateway endpoints | Payments Revamp | DONE | LOW | — |
| Redesign rider earnings screen | Rider App Redesign | TODO | HIGH | Arjun Sharma |
| Fix map rendering lag on low-end devices | Rider App Redesign | IN_PROGRESS | MEDIUM | — |
| Build delivery heatmap query | Analytics Dashboard | DONE | HIGH | Rohan Verma |

---

## API Reference

All protected endpoints require `Authorization: Bearer <token>` header.

### Auth

```bash
# Register
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Nishchay","email":"nishchay@test.com","password":"password123"}'

# Login
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"arjun@example.com","password":"password123"}'
```

### Projects

```bash
# List projects (paginated)
curl -s "http://localhost:8080/projects?page=1&limit=20" \
  -H "Authorization: Bearer <token>"

# Create project
curl -s -X POST http://localhost:8080/projects \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Project","description":"Optional description"}'

# Get project
curl -s http://localhost:8080/projects/<project-id> \
  -H "Authorization: Bearer <token>"

# Update project (owner only)
curl -s -X PATCH http://localhost:8080/projects/<project-id> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Name"}'

# Delete project (owner only, cascades tasks)
curl -s -X DELETE http://localhost:8080/projects/<project-id> \
  -H "Authorization: Bearer <token>"
```

### Tasks

```bash
# List tasks (paginated, filterable)
curl -s "http://localhost:8080/projects/<project-id>/tasks?status=IN_PROGRESS&page=1&limit=20" \
  -H "Authorization: Bearer <token>"

# Create task (owner only)
curl -s -X POST http://localhost:8080/projects/<project-id>/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Fix bug","priority":"HIGH","dueDate":"2025-12-31"}'

# Update task (owner or assignee)
curl -s -X PATCH http://localhost:8080/tasks/<task-id> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'

# Delete task (owner or creator)
curl -s -X DELETE http://localhost:8080/tasks/<task-id> \
  -H "Authorization: Bearer <token>"
```

### Stats

```bash
# Project stats
curl -s http://localhost:8080/projects/<project-id>/stats \
  -H "Authorization: Bearer <token>"
```

Response:
```json
{
  "byStatus": { "TODO": 2, "IN_PROGRESS": 1, "DONE": 1 },
  "byAssignee": [
    { "userId": "uuid", "name": "Priya Mehta", "count": 1 },
    { "userId": "uuid", "name": "Rohan Verma", "count": 1 },
    { "userId": null, "name": "Unassigned", "count": 1 }
  ],
  "total": 3
}
```

### Pagination response shape

All list endpoints return:
```json
{
  "data": [...],
  "page": 1,
  "limit": 20,
  "total": 47,
  "totalPages": 3
}
```

Defaults: `page=1`, `limit=20`. Maximum `limit=100`. Values below 1 are clamped to 1.

---

## HTTP Status Codes

| Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Created |
| 204 | Deleted (no body) |
| 400 | Validation failed — check `fields` in response |
| 401 | Not authenticated — missing or invalid token |
| 403 | Authenticated but not authorized |
| 404 | Resource not found |
| 409 | Conflict — duplicate email |
| 500 | Internal server error |

Error response shape:
```json
{
  "error": "validation_failed",
  "fields": {
    "title": "title is required"
  }
}
```

---

## Architecture Decisions

### Package structure: package-by-feature

Chose package-by-feature (`auth/`, `project/`, `task/`) over package-by-layer (`controller/`, `service/`, `repository/`). Each feature is self-contained — you can read, understand, and modify a feature without jumping across the codebase. Package-by-layer creates artificial coupling between unrelated classes that happen to share a technical role.

### Flyway over Liquibase

Flyway uses plain SQL migrations — no XML DSL to learn, no abstraction over the database. SQL is what gets executed, so that's what you write. Liquibase's abstraction is useful for multi-database portability, but this project targets PostgreSQL exclusively. Flyway also fails fast on checksum mismatch, making it harder to accidentally corrupt migration history.

### Gradle Kotlin DSL over Maven

Kotlin DSL gives compile-time safety on build configuration. IDE autocomplete works on build scripts. Maven's XML is verbose and error-prone for anything beyond simple dependency declarations. Gradle's incremental builds are also significantly faster on repeated builds.

### JWT over sessions

Stateless JWT fits a containerized API with no sticky sessions. No session store needed. Each token carries `userId` and `email` claims, signed with HS256. Secret is loaded from environment — never hardcoded. Expiry is 24 hours, configurable via `JWT_EXPIRATION_MS`.

### BCrypt cost 12

Cost 10 is Spring Security's default and is widely considered the minimum for production in 2024. Cost 14 adds noticeable latency per login (~1-2 seconds). Cost 12 is the standard production choice — slow enough to deter brute force, fast enough not to impact user experience. Test profile uses cost 4 to keep test suite runtime reasonable.

### PATCH semantics: `Patch<T>` not `Optional<T>`

Partial updates need to distinguish three states: field absent (don't touch), field present with value (update), field present as null (clear). `Optional<T>` cannot represent this — `Optional.ofNullable(null)` and a missing field both produce `Optional.empty()`.

Implemented a custom `Patch<T>` wrapper with a bespoke Jackson deserializer:
- `getAbsentValue()` fires when field is missing from JSON → returns `Patch.absent()`
- `getNullValue()` fires when field is present as null → returns `Patch.of(null)`
- `deserialize()` fires when field has a value → returns `Patch.of(value)`

Without overriding `getAbsentValue()`, Jackson falls back to `getNullValue()` for absent fields — causing absent fields to be treated as explicit nulls, silently clearing data that wasn't meant to be touched. This was caught and fixed during development.

### JOIN FETCH strategy

Nullable foreign keys use `LEFT JOIN FETCH`, non-nullable use `JOIN FETCH`:
- `assignee` is nullable (task may be unassigned) → `LEFT JOIN FETCH` — inner join would silently exclude unassigned tasks
- `creator` is non-nullable (every task has a creator) → `JOIN FETCH`

Getting this wrong causes silent data loss that's very hard to debug.

### Authorization model

| Action | Who |
|---|---|
| Create project | Any authenticated user |
| Update/delete project | Project owner only |
| Create task | Project owner only |
| Update task | Project owner OR task assignee |
| Delete task | Project owner OR task creator |
| List tasks | Any authenticated user (open read, no membership concept) |

List tasks is intentionally open-read. Restricting it would require a project membership model — a deliberate scope decision documented below.

### Cascade delete

Project deletion cascades to tasks via SQL `ON DELETE CASCADE` on the `tasks.project_id` foreign key, not JPA cascade. SQL cascade is more reliable — it fires at the database level regardless of how the deletion is triggered, and doesn't require loading child entities into memory.

### Spring Security 401 vs 403

Spring Security's default `AuthenticationEntryPoint` returns 403 for unauthenticated requests instead of 401 — a well-known gotcha. Explicitly configured a custom entry point returning 401 JSON for unauthenticated requests and a custom `AccessDeniedHandler` returning 403 JSON for authorized-but-forbidden requests.

### Structured logging

Plain text logging locally, JSON logging in Docker (via `logstash-logback-encoder`, activated by `spring.profiles.active=docker`). JSON is machine-readable for log aggregation tools (Datadog, ELK, Loki). Every log line in Docker includes `userId` from MDC, set by `MdcFilter` on each request and cleared in a `finally` block to prevent thread pool contamination.

### Graceful shutdown

`server.shutdown=graceful` with `spring.lifecycle.timeout-per-shutdown-phase=30s`. On `SIGTERM` (e.g. `docker compose down`), Spring stops accepting new requests and waits up to 30 seconds for in-flight requests to complete before shutting down.

---

## What I'd Do With More Time

**Token blacklist (Redis)** — JWTs are stateless, so there's no way to invalidate a token before expiry. A Redis-backed blacklist would let the system immediately revoke tokens on logout, user ban, or password change. The 24-hour expiry window is the current vulnerability.

**Refresh token rotation** — issue short-lived access tokens (15 minutes) with longer-lived refresh tokens. Rotation means a stolen refresh token can only be used once before it's invalidated.

**Rate limiting on auth endpoints** — `POST /auth/login` and `POST /auth/register` are currently unthrottled. Redis-backed rate limiting (e.g. Bucket4j) would prevent brute-force attacks on login and registration spam.

**Project membership model** — the current authorization model is owner-only for task creation. A real team tool needs a `project_members` join table, "add member" / "remove member" endpoints, and membership checks on every task operation. This was deliberately deferred — implementing it half-baked would be worse than documenting it as future work.

**Field-level update restrictions per role** — currently any owner or assignee can update any field on a task. A more refined model would restrict assignees to updating only `status` (not `priority`, `dueDate`, or `assigneeId`), keeping those fields owner-only.

**Soft deletes** — `deleted_at` timestamp instead of hard deletes. Supports audit history, accidental deletion recovery, and compliance requirements.

**Audit logging** — a separate `audit_log` table recording who changed what and when. Useful for compliance and debugging production issues.

**OpenAPI / Swagger docs** — `springdoc-openapi` would auto-generate interactive API docs from annotations. Better developer experience than a Postman collection for external consumers.

**CI/CD pipeline** — GitHub Actions running `./gradlew clean test` on every PR, Docker build on merge to main, and deployment to a staging environment.

**Caching with Redis** — project and task listings are read-heavy. Redis caching on list endpoints with cache invalidation on write would significantly reduce DB load at scale.