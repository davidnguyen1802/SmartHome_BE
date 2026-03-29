# SmartHome Backend - FE API Testing Guide

This guide helps Frontend test the current backend APIs and verify visible results.

## 1) Quick context from codebase

- Base URL: `http://localhost:8080`
- API prefix: `/api/v1`
- Auth: login API available at `/api/v1/auth/login`; current routes are still `permitAll()`
- Response wrapper for success and error:

```json
{
  "statusCode": 200,
  "message": "...",
  "data": {}
}
```

## 2) Prerequisites

### Required env vars (from `application.yaml`)

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `ADAFRUIT_IO_USERNAME`
- `ADAFRUIT_IO_KEY`

Optional auth seed vars:

- `AUTH_SEED_USERNAME` (default `admin`)
- `AUTH_SEED_PASSWORD` (default `admin123`)

> Note: API tests below can run without real MQTT hardware if you use `/api/v1/test/sensors/ingest`.

### Required seed data in DB

`Dashboard` and `Device` APIs expect existing rows. If missing, backend can return 400 with messages like `Missing device state` or `Missing sensor latest`.

You need at least:

- `device_states`: `LED`, `FAN`
- `automation_configs`: row with `id = 1`
- `sensor_latest`: `TEMP`, `HUMI`, `LIGHT`, `PIR`

## 3) API catalog for FE

### 3.0 Login

- **POST** `/api/v1/auth/login`
- Body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

- Returns `data.accessToken`, `data.tokenType`, `data.expiresAt`, `data.username`

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

### 3.1 Dashboard

- **GET** `/api/v1/dashboard`
- Purpose: one-shot snapshot for FE home screen
- `data` shape:
  - `temp`, `humi`, `light`, `pir` -> sensor latest
  - `led`, `fan` -> device status
  - `automationConfig` -> automation thresholds

Example:

```bash
curl -X GET "http://localhost:8080/api/v1/dashboard"
```

### 3.2 Device status

- **GET** `/api/v1/devices/{deviceType}`
- `deviceType`: `LED` or `FAN`

```bash
curl -X GET "http://localhost:8080/api/v1/devices/LED"
curl -X GET "http://localhost:8080/api/v1/devices/FAN"
```

### 3.3 Set device mode

- **PUT** `/api/v1/devices/{deviceType}/mode`
- Body:

```json
{
  "mode": "MANUAL"
}
```

- `mode`: `MANUAL` or `AUTO`

```bash
curl -X PUT "http://localhost:8080/api/v1/devices/LED/mode" \
  -H "Content-Type: application/json" \
  -d "{\"mode\":\"AUTO\"}"
```

### 3.4 Manual command

- **POST** `/api/v1/devices/{deviceType}/command`
- Body:

```json
{
  "state": "ON",
  "reason": "test mqtt"
}
```

- `state`: `ON` or `OFF`

```bash
curl -X POST "http://localhost:8080/api/v1/devices/LED/command" \
  -H "Content-Type: application/json" \
  -d "{\"state\":\"ON\",\"reason\":\"test mqtt\"}"
```

### 3.5 Automation config

- **GET** `/api/v1/automation/config`

```bash
curl -X GET "http://localhost:8080/api/v1/automation/config"
```

- **PUT** `/api/v1/automation/fan-threshold`
- Body:

```json
{
  "lowTemp": 26,
  "highTemp": 30
}
```

Rules:

- each value must be in `[0,100]`
- `highTemp >= lowTemp`

```bash
curl -X PUT "http://localhost:8080/api/v1/automation/fan-threshold" \
  -H "Content-Type: application/json" \
  -d "{\"lowTemp\":26,\"highTemp\":30}"
```

### 3.6 Sensor ingest (test helper for FE)

- **POST** `/api/v1/test/sensors/ingest`
- Purpose: simulate sensor data from FE without waiting for MQTT device

Body:

```json
{
  "sensorType": "TEMP",
  "value": 32
}
```

- `sensorType`: `TEMP`, `HUMI`, `LIGHT`, `PIR`

```bash
curl -X POST "http://localhost:8080/api/v1/test/sensors/ingest" \
  -H "Content-Type: application/json" \
  -d "{\"sensorType\":\"LIGHT\",\"value\":20}"
```

## 4) End-to-end test flows FE can run

## Flow A - Manual control visible on dashboard

1. Set LED mode to `MANUAL`.
2. Send command `LED ON` with reason.
3. Call `GET /api/v1/devices/LED` and `GET /api/v1/dashboard`.
4. Expect:
   - `led.state = ON`
   - `led.mode = MANUAL`
   - `led.lastCommandSource = MANUAL_USER`
   - `led.lastCommandReason` equals sent reason

## Flow B - Auto LED by LIGHT sensor

1. Set LED mode to `AUTO`.
2. Ingest `LIGHT = 20`.
3. Read dashboard: expect LED turns `ON`.
4. Ingest `LIGHT = 90`.
5. Read dashboard: expect LED turns `OFF`.

Current backend thresholds in automation logic:

- `LIGHT <= 50` -> LED `ON`
- `LIGHT >= 70` -> LED `OFF`

## Flow C - Auto FAN by TEMP sensor

1. Set FAN mode to `AUTO`.
2. Read current fan thresholds from `GET /api/v1/automation/config`.
3. Ingest `TEMP` below/equal `fanLowTemp` -> expect FAN `OFF`.
4. Ingest `TEMP` above/equal `fanHighTemp` -> expect FAN `ON`.

## Flow D - Validation behavior

Try invalid request examples:

- invalid enum:

```json
{
  "mode": "AUTOO"
}
```

- invalid threshold relation:

```json
{
  "lowTemp": 35,
  "highTemp": 30
}
```

Expected error shape:

```json
{
  "statusCode": 400,
  "message": "...",
  "data": null
}
```

## 5) Payload enums FE should use

- `DeviceType`: `LED`, `FAN`
- `DeviceMode`: `MANUAL`, `AUTO`
- `DeviceState`: `ON`, `OFF`
- `SensorType`: `TEMP`, `HUMI`, `LIGHT`, `PIR`

## 6) Notes for FE integration

- Use exact uppercase enum strings.
- Backend currently has no auth requirement, but keep auth layer flexible for future JWT enablement.
- No explicit CORS config is present in backend; if FE runs on another origin and browser blocks requests, add backend CORS config.

