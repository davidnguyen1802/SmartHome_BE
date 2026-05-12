# SmartHome Backend

## Overview

Spring Boot 4.0.3 IoT backend (Java 21) for a smart home system. Ingests sensor data via MQTT (Adafruit IO), applies automation rules, controls devices (LED/FAN), and exposes a JWT-secured REST API with SSE real-time updates.

## Build & Run

```bash
# Build
./mvnw clean package

# Build (skip tests)
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run

# Run tests
./mvnw test

# Docker
docker build -t smarthome-backend:latest .
docker-compose -f deploy/compute-engine/docker-compose.yml up
```

## Project Structure

```
src/main/java/com/DANN/SmartHome/
  config/          # Spring configs (Security, MQTT, JWT filter)
  controller/      # REST endpoints (Auth, Dashboard, Device, Automation)
  DTO/             # Request/Response DTOs, Internal event objects
  domain/
    entity/        # JPA entities (UserEntity, DeviceStateEntity, SensorLatest, etc.)
    enums/         # DeviceType, DeviceMode, DeviceState, SensorType, CommandSource
    repository/    # Spring Data JPA repositories
  mapper/          # MapStruct mappers (entity <-> DTO)
  mqtt/            # MQTT message handler (MqttSensorMessageHandler)
  service/         # Service interfaces
    Imp/           # Service implementations
    event/         # Spring application events
  Exception/       # Global exception handler, custom exceptions
  Util/            # Response utilities
```

## Tech Stack

- **Java 21** + **Spring Boot 4.0.3**
- **PostgreSQL** (via Spring Data JPA / Hibernate)
- **MQTT** (Eclipse Paho + Spring Integration) connecting to Adafruit IO broker
- **Spring Security** (JWT authentication, BCrypt passwords, stateless sessions)
- **MapStruct** (compile-time DTO mapping) + **Lombok** (boilerplate reduction)
- **Maven 3.9.12** (via wrapper)
- **Docker** (multi-stage build, non-root runtime)

## Environment Variables (Required)

```
DB_URL=jdbc:postgresql://host:port/db?sslmode=require
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET_KEY=...          # 64+ char hex for HMAC-SHA256
ADAFRUIT_IO_USERNAME=...
ADAFRUIT_IO_KEY=...
```

Optional: `PORT` (default 8080), `CORS_ALLOWED_ORIGIN` (default http://localhost:3000), `AUTH_SEED_USERNAME`, `AUTH_SEED_PASSWORD`.

## Key Architecture Patterns

- **Layered architecture**: Controller -> Service -> Repository -> PostgreSQL
- **MQTT inbound flow**: Adafruit IO -> Spring Integration -> MqttSensorMessageHandler -> SensorIngestionService -> AutomationService -> DeviceCommandPublisher -> MQTT outbound
- **SSE streaming**: DashboardRealtimeService broadcasts state changes via Server-Sent Events
- **Spring Events**: DashboardChangedEvent decouples state changes from SSE broadcasting
- **Service interface + Imp pattern**: All services have interface + implementation in `service/Imp/`
- **Global exception handling**: `@RestControllerAdvice` in `Exception/GlobalExceptionHandler`
- **Consistent responses**: All endpoints return `BaseResponse<T>` wrapper via `ResponseCentral`

## API Endpoints

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /auth/login | No | Login, returns JWT + refresh token |
| POST | /auth/refresh | No | Refresh access token |
| GET | /auth/me | Yes | Current user profile |
| GET | /dashboard | Yes | Full sensor + device state snapshot |
| GET | /dashboard/stream | Yes | SSE real-time updates |
| GET | /devices/{type} | Yes | Device status (LED or FAN) |
| PUT | /devices/{type}/mode | Yes | Set device mode (MANUAL/AUTO) |
| POST | /devices/{type}/command | Yes | Send command (ON/OFF) |
| GET | /automation/config | Yes | Get automation thresholds |
| PUT | /automation/fan-threshold | Yes | Update fan temperature thresholds |
| POST | /test/sensors/ingest | Yes | Test endpoint: inject sensor data |

## Conventions

- **DTOs**: Request DTOs in `DTO/request/`, Response DTOs in `DTO/response/`, Internal events in `DTO/Internal/`
- **Enums**: Type-safe enums in `domain/enums/` for DeviceType, SensorType, DeviceMode, DeviceState, CommandSource, NotificationType
- **Naming**: Entity classes suffixed with `Entity`, service implementations suffixed with `Imp`
- **Config properties**: Adafruit MQTT settings bound via `@ConfigurationProperties` in `AdafruitProperties`
- **Database**: DDL managed externally (`ddl-auto: none`), not by Hibernate auto-generation
- **Tests**: Located in `src/test/java/com/DANN/SmartHome/`, use JUnit 5 + Mockito + Spring Test

## Important Notes

- `.env` file contains secrets -- never commit to git
- MQTT broker is Adafruit IO over SSL (port 8883)
- Default admin user is seeded on startup when `auth.seed.enabled=true`
- CORS is configured for a single origin (frontend app)
- Deployment targets Google Compute Engine (see `docs/deploy-compute-engine.md`)

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **SmartHome_BE** (666 symbols, 1574 relationships, 52 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/SmartHome_BE/context` | Codebase overview, check index freshness |
| `gitnexus://repo/SmartHome_BE/clusters` | All functional areas |
| `gitnexus://repo/SmartHome_BE/processes` | All execution flows |
| `gitnexus://repo/SmartHome_BE/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->

## Agent Protocol

See `AGENTS.md` for the Zero-Trust skill loading protocol (applies to all AI agents) and the MCP runtime-enforcement section (when enabled).

## Self-Learning Protocol

At the end of any multi-step task with user corrections, load and run **[common/common-session-retrospective](.claude/skills/common/common-session-retrospective/SKILL.md)** to capture skill gaps and prevent repeat rework.
