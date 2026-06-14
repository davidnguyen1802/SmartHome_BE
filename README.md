# SmartHome Backend

SmartHome Backend là một hệ thống backend Spring Boot phục vụ cho ứng dụng IoT smart home, hỗ trợ thu thập dữ liệu cảm biến từ Adafruit IO, điều khiển thiết bị và cung cấp realtime dashboard cho frontend.

SmartHome Backend is a Spring Boot backend system for smart home IoT applications, supporting sensor data collection from Adafruit IO, device control, and realtime dashboard delivery for the frontend.

## Tổng quan dự án / Project Overview

Dự án này tập trung vào ba nhiệm vụ chính:

- Thu thập và xử lý dữ liệu cảm biến theo thời gian thực qua MQTT.
- Quản lý trạng thái thiết bị và điều khiển automation logic.
- Cung cấp realtime data stream cho frontend bằng SSE để giảm thiểu polling HTTP.

This project focuses on three main goals:

- Collect and process sensor telemetry in real time via MQTT.
- Manage device state and automation logic for smart home control.
- Deliver realtime data streams to the frontend using SSE to reduce HTTP polling overhead.

## Điểm nổi bật / Key Highlights

- Spring Boot + Java 21 cho backend API hiện đại.
- MQTT integration với Adafruit IO để nhận telemetry và gửi lệnh điều khiển.
- SSE realtime dashboard cho trải nghiệm cập nhật dữ liệu tức thời.
- PostgreSQL để lưu sensor readings, latest sensor state và device command history.
- JWT authentication với refresh token rotation cho bảo mật API.

- Spring Boot + Java 21 for a modern backend API.
- MQTT integration with Adafruit IO for telemetry ingestion and device command publishing.
- SSE realtime dashboard for instant data updates.
- PostgreSQL for sensor readings, latest device states, and command history.
- JWT authentication with refresh token rotation for API security.

## Công nghệ sử dụng / Tech Stack

- Java 21
- Spring Boot 4.0.3
- Spring Web / Spring Security / Spring Data JPA
- Spring Integration MQTT
- PostgreSQL
- SSE (Server-Sent Events)
- Maven

## Kiến trúc hệ thống / System Architecture

1. MQTT inbound
   - Kết nối tới Adafruit IO để nhận dữ liệu cảm biến từ các feed như TEMP, HUMI, LIGHT, PIR.
2. Sensor ingestion service
   - Validate dữ liệu, lưu vào bảng sensor_readings và sensor_latest.
3. Automation & device control
   - Dựa trên ngưỡng cảm biến để kích hoạt logic điều khiển LED/FAN.
4. SSE dashboard
   - Gửi snapshot và heartbeat tới frontend theo thời gian thực.

1. MQTT inbound
   - Connects to Adafruit IO to receive sensor data from feeds such as TEMP, HUMI, LIGHT, and PIR.
2. Sensor ingestion service
   - Validates incoming data and stores it in sensor_readings and sensor_latest tables.
3. Automation & device control
   - Uses sensor thresholds to trigger LED/FAN automation logic.
4. SSE dashboard
   - Pushes snapshots and heartbeat events to the frontend in real time.

## Chức năng chính / Core Features

### 1. Authentication / Xác thực

- POST `/api/v1/auth/login`
- POST `/api/v1/auth/refresh`
- GET `/api/v1/auth/me`

### 2. Dashboard / Bảng điều khiển

- GET `/api/v1/dashboard` → lấy snapshot trạng thái hiện tại
- GET `/api/v1/dashboard/stream` → realtime SSE stream

### 3. Device control / Điều khiển thiết bị

- GET `/api/v1/devices/{deviceType}`
- PUT `/api/v1/devices/{deviceType}/mode`
- POST `/api/v1/devices/{deviceType}/command`

### 4. Automation / Tự động hóa

- GET `/api/v1/automation/config`
- PUT `/api/v1/automation/fan-threshold`

### 5. Testing / Demo

- POST `/api/v1/test/sensors/ingest`

## Cài đặt nhanh / Quick Start

### Yêu cầu / Requirements

- Java 21
- Maven
- PostgreSQL
- Tài khoản Adafruit IO và các biến môi trường liên quan

### Biến môi trường cần thiết / Required Environment Variables

```env
DB_URL=...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
ADAFRUIT_IO_USERNAME=...
ADAFRUIT_IO_KEY=...
CORS_ALLOWED_ORIGIN=http://localhost:3000
```

### Chạy project / Run the project

```bash
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy tại:

```text
http://localhost:8080
```

The application will run at:

```text
http://localhost:8080
```

## Quick test / Kiểm tra nhanh

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## Deploy / Triển khai

Project đã được chuẩn bị cho deployment trên Google Compute Engine bằng Docker.

The project is ready for deployment on Google Compute Engine using Docker.

Hướng dẫn chi tiết:

- `docs/deploy-compute-engine.md`
- `deploy/compute-engine/`

## Lưu ý cho frontend / Frontend Notes

- SSE là realtime channel chính cho dashboard.
- Nếu frontend dùng native `EventSource`, cần có phương án inject JWT header (ví dụ polyfill hoặc fallback polling).

- SSE is the main realtime channel for the dashboard.
- If the frontend uses native `EventSource`, it should support JWT header injection (for example via polyfill or fallback polling).

## Kết luận / Conclusion

Đây là một backend IoT hoàn chỉnh, phù hợp cho mô hình smart home dashboard, có thể kết nối trực tiếp với frontend để hiển thị dữ liệu cảm biến, điều khiển thiết bị và theo dõi trạng thái realtime.

This is a complete IoT backend solution suitable for smart home dashboards, capable of connecting directly to frontend applications to display sensor data, control devices, and monitor realtime status.

