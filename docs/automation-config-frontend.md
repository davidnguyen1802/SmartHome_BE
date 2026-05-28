# Automation Config — FE Integration Guide (LED & FAN)

> **Audience:** Frontend devs đang thiết kế lại màn hình "Automation / Cài đặt tự động" cho LED và FAN.
> **Backend version:** Spring Boot 4.0.3, base URL `/api/v1`, JWT Bearer auth.
> **Last updated:** 2026-05-10

---

## 1. Bức tranh tổng quan

Hệ thống có 2 thiết bị: **LED** và **FAN**. Mỗi thiết bị có 2 chế độ độc lập:

| Mode | Hành vi |
|------|---------|
| `MANUAL` | User bấm nút ON/OFF từ UI. Backend không tự đổi state. |
| `AUTO` | Backend tự ON/OFF dựa trên dữ liệu cảm biến **và** ngưỡng được cấu hình ở `automation_config`. |

Tài liệu này tập trung vào màn hình **cấu hình ngưỡng AUTO** (threshold) cho LED và FAN.

> ⚠️ **Quan trọng cho FE:** ngưỡng chỉ có tác dụng khi thiết bị đang ở mode `AUTO`. UI nên hiển thị rõ trạng thái mode hiện tại để user hiểu vì sao bấm "Save threshold" mà thiết bị không phản ứng (vì đang `MANUAL`).

---

## 2. Mô hình ngưỡng (Threshold logic) — phải hiểu trước khi vẽ UI

Cả 2 thiết bị đều dùng cơ chế **hysteresis** (2 ngưỡng) để tránh thiết bị bật/tắt liên tục khi sensor dao động quanh 1 ngưỡng đơn.

### 2.1 FAN — điều khiển theo nhiệt độ (`TEMP`)

| Field | Ý nghĩa |
|-------|---------|
| `fanLowTemp` | Khi `TEMP <= fanLowTemp` → FAN tắt (đủ mát rồi) |
| `fanHighTemp` | Khi `TEMP >= fanHighTemp` → FAN bật (nóng quá) |
| Vùng `(fanLowTemp, fanHighTemp)` | FAN giữ nguyên state trước đó (vùng dead-band, chống flapping) |

**Ràng buộc:** `fanHighTemp >= fanLowTemp`. Đơn vị: °C. Range hợp lệ `0.0 – 100.0`.

**Ví dụ:** `fanLowTemp = 26`, `fanHighTemp = 30`:
- 25°C → FAN OFF
- 28°C → giữ nguyên (không trigger)
- 31°C → FAN ON
- 29°C → vẫn ON (chưa đủ mát để tắt)
- 25°C → FAN OFF

### 2.2 LED — điều khiển theo cường độ ánh sáng (`LIGHT`)

> Logic **ngược chiều** với FAN: LED bật khi tối, tắt khi sáng.

| Field | Ý nghĩa |
|-------|---------|
| `ledOnThreshold` | Khi `LIGHT <= ledOnThreshold` → LED bật (đủ tối) |
| `ledOffThreshold` | Khi `LIGHT >= ledOffThreshold` → LED tắt (đủ sáng) |
| Vùng `(ledOnThreshold, ledOffThreshold)` | LED giữ nguyên state trước đó |

**Ràng buộc:** `ledOffThreshold >= ledOnThreshold`. Đơn vị: giá trị thô từ Adafruit IO `light` feed (thang `0.0 – 100.0`, càng cao càng sáng). Range hợp lệ `0.0 – 100.0`.

**Ví dụ:** `ledOnThreshold = 50`, `ledOffThreshold = 70`:
- LIGHT = 40 → LED ON (tối)
- LIGHT = 60 → giữ nguyên
- LIGHT = 80 → LED OFF (sáng)
- LIGHT = 65 → vẫn OFF (chưa đủ tối để bật lại)
- LIGHT = 45 → LED ON

### 2.3 Trực quan hoá (gợi ý cho UI)

```
FAN (theo nhiệt độ)
  OFF zone           dead-band            ON zone
─────────────●───────────────────●─────────────►  TEMP (°C)
              fanLowTemp        fanHighTemp

LED (theo ánh sáng)
   ON zone            dead-band           OFF zone
─────────────●───────────────────●─────────────►  LIGHT
            ledOnThreshold   ledOffThreshold
```

UI nên dùng **dual-handle range slider** (2 nút trên cùng 1 thanh) để user kéo cả 2 ngưỡng cùng lúc và thấy trực quan vùng dead-band.

---

## 3. APIs cần dùng

### 3.1 Convention chung

- **Base URL:** `/api/v1`
- **Auth:** tất cả endpoint dưới đây yêu cầu header `Authorization: Bearer <accessToken>`. Token lấy từ `POST /api/v1/auth/login`.
- **Content-Type:** `application/json`
- **Response wrapper:** mọi response (kể cả lỗi) đều bọc trong:

```json
{
  "statusCode": 200,
  "message": "Get automation config successfully",
  "data": { /* payload */ }
}
```

→ FE nên có 1 axios/fetch interceptor unwrap `response.data.data` cho 2xx và đọc `response.data.message` cho error toast.

---

### 3.2 `GET /api/v1/automation/config` — Lấy ngưỡng hiện tại

**Request:** không có body.

**Response 200:**

```json
{
  "statusCode": 200,
  "message": "Get automation config successfully",
  "data": {
    "fanLowTemp": 26.00,
    "fanHighTemp": 30.00,
    "ledOnThreshold": 50.00,
    "ledOffThreshold": 70.00,
    "pirAlertCooldownSeconds": 60
  }
}
```

| Field | Type | Notes |
|-------|------|-------|
| `fanLowTemp` | `number` (decimal, 2dp) | °C |
| `fanHighTemp` | `number` (decimal, 2dp) | °C |
| `ledOnThreshold` | `number` (decimal, 2dp) | đơn vị light feed |
| `ledOffThreshold` | `number` (decimal, 2dp) | đơn vị light feed |
| `pirAlertCooldownSeconds` | `integer` | Cooldown giữa 2 thông báo PIR (không thuộc UI LED/FAN, có thể ẩn) |

→ Gọi khi mount màn hình "Automation Settings" để pre-fill 2 slider.

---

### 3.3 `PUT /api/v1/automation/fan-threshold` — Cập nhật ngưỡng FAN

**Request body:**

```json
{
  "lowTemp": 26.0,
  "highTemp": 30.0
}
```

**Validation (backend enforced):**

| Rule | HTTP code khi vi phạm | Message ví dụ |
|------|------------------------|----------------|
| Cả 2 field không null | 400 | `lowTemp: must not be null` |
| `0.0 <= lowTemp <= 100.0` | 400 | `lowTemp: must be less than or equal to 100.0` |
| `0.0 <= highTemp <= 100.0` | 400 | `highTemp: must be less than or equal to 100.0` |
| `highTemp >= lowTemp` | 400 | `highTemp must be greater than or equal to lowTemp` |

**Response 200:** trả về toàn bộ `AutomationConfigResponse` đã cập nhật (giống shape `GET /config`), `message: "Update fan threshold successfully"`.

**Side effect:** backend đẩy snapshot mới qua SSE `dashboard.snapshot` (xem mục 3.6). FE đang nghe SSE sẽ tự nhận update — không cần re-fetch thủ công.

---

### 3.4 `PUT /api/v1/automation/led-threshold` — Cập nhật ngưỡng LED *(mới)*

**Request body:**

```json
{
  "onThreshold": 50.0,
  "offThreshold": 70.0
}
```

**Validation (backend enforced):**

| Rule | HTTP code | Message ví dụ |
|------|-----------|----------------|
| Cả 2 field không null | 400 | `onThreshold: must not be null` |
| `0.0 <= onThreshold <= 100.0` | 400 | `onThreshold: must be less than or equal to 100.0` |
| `0.0 <= offThreshold <= 100.0` | 400 | `offThreshold: must be less than or equal to 100.0` |
| `offThreshold >= onThreshold` | 400 | `offThreshold must be greater than or equal to onThreshold` |

**Response 200:** giống shape `AutomationConfigResponse`, `message: "Update led threshold successfully"`.

**Side effect:** giống fan — phát SSE realtime.

---

### 3.5 Endpoints liên quan (FE cần biết để build flow đầy đủ)

| Method | Path | Mục đích | Body / Param |
|--------|------|---------|--------------|
| `GET` | `/api/v1/devices/LED` | Lấy mode + state + lastCommand của LED | – |
| `GET` | `/api/v1/devices/FAN` | Lấy mode + state + lastCommand của FAN | – |
| `PUT` | `/api/v1/devices/LED/mode` | Đổi mode LED (cần cho user bật `AUTO` để threshold có tác dụng) | `{ "mode": "AUTO" \| "MANUAL" }` |
| `PUT` | `/api/v1/devices/FAN/mode` | Đổi mode FAN | `{ "mode": "AUTO" \| "MANUAL" }` |
| `POST` | `/api/v1/devices/LED/command` | Bấm thủ công ON/OFF (chỉ áp dụng khi mode = `MANUAL`) | `{ "state": "ON" \| "OFF", "reason": "string?" }` |
| `POST` | `/api/v1/devices/FAN/command` | Bấm thủ công ON/OFF | `{ "state": "ON" \| "OFF", "reason": "string?" }` |
| `GET` | `/api/v1/dashboard` | Snapshot toàn bộ (sensor + 2 device + automationConfig) | – |
| `GET` | `/api/v1/dashboard/stream` | SSE realtime — chi tiết mục 3.6 | – |

**`DeviceStatusResponse` shape** (dùng cho cả `GET /devices/{type}` và bên trong dashboard):

```json
{
  "deviceType": "LED",
  "mode": "AUTO",
  "state": "ON",
  "lastCommandPayload": "0",
  "lastCommandSource": "AUTOMATION",
  "lastCommandReason": "LIGHT <= onThreshold",
  "lastCommandAt": "2026-05-10T03:34:12.123Z",
  "updatedAt": "2026-05-10T03:34:12.456Z"
}
```

| Enum | Values |
|------|--------|
| `DeviceType` | `LED`, `FAN` |
| `DeviceMode` | `MANUAL`, `AUTO` |
| `DeviceState` | `ON`, `OFF` |
| `CommandSource` | `MANUAL_USER`, `AUTOMATION`, `SYSTEM` |

→ FE có thể hiển thị `lastCommandReason` + `lastCommandSource` để user thấy "vì sao thiết bị vừa bật/tắt" (debug-friendly + trust-building).

---

### 3.6 SSE realtime — `GET /api/v1/dashboard/stream`

- **Content-Type:** `text/event-stream`
- **Auth:** vẫn cần JWT. Lưu ý: native `EventSource` của browser **không gửi custom header** → FE thường dùng [`event-source-polyfill`](https://www.npmjs.com/package/event-source-polyfill) hoặc fetch streaming để inject `Authorization: Bearer ...`.
- **Events emit:**
  - `dashboard.snapshot` — payload là full `DashboardResponse` (xem 3.6.1). Phát ra:
    - Ngay khi client subscribe (initial snapshot)
    - Mỗi lần state thay đổi: sensor mới về, device đổi state, automation config được update.
  - `heartbeat` — payload chuỗi `"ok"`, mặc định mỗi 15s. FE dùng để detect mất kết nối.

**3.6.1 `DashboardResponse` payload:**

```json
{
  "temp":  { "sensorType": "TEMP",  "value": 28.5, "receivedAt": "2026-05-10T03:34:00Z" },
  "humi":  { "sensorType": "HUMI",  "value": 65.0, "receivedAt": "2026-05-10T03:34:00Z" },
  "light": { "sensorType": "LIGHT", "value": 42.0, "receivedAt": "2026-05-10T03:34:01Z" },
  "pir":   { "sensorType": "PIR",   "value": 0.0,  "receivedAt": "2026-05-10T03:33:45Z" },
  "led":   { /* DeviceStatusResponse */ },
  "fan":   { /* DeviceStatusResponse */ },
  "automationConfig": {
    "fanLowTemp": 26.0, "fanHighTemp": 30.0,
    "ledOnThreshold": 50.0, "ledOffThreshold": 70.0,
    "pirAlertCooldownSeconds": 60
  }
}
```

→ FE chỉ cần subscribe 1 lần cho cả màn dashboard + automation. Khi user save threshold, snapshot mới sẽ tự về — FE không cần optimistic update phức tạp.

---

## 4. Error contract

Backend dùng `BaseResponse` wrapper cho lỗi (không phải RFC 7807 ProblemDetails). Mọi error response có shape:

```json
{
  "statusCode": 400,
  "message": "highTemp must be greater than or equal to lowTemp",
  "data": null
}
```

| HTTP status | Khi nào | Cách xử lý ở FE |
|-------------|---------|------------------|
| `400` | Validation fail (`@NotNull`, `@DecimalMin/Max`, business rule), JSON malformed, type mismatch path/query | Show inline form error theo `message`. Field cụ thể parse từ prefix (`"lowTemp: must not be null"` → field `lowTemp`). |
| `401` | Token thiếu/invalid/hết hạn | Trigger refresh token flow → `POST /api/v1/auth/refresh`, retry. Nếu vẫn 401 → logout. |
| `403` | (hiếm) | Show toast "Bạn không có quyền". |
| `404` | Sai path | – |
| `500` | Unhandled error | Show toast generic "Có lỗi xảy ra, thử lại sau". |

> **Note:** message từ `MethodArgumentNotValidException` có dạng `"<fieldName>: <constraint message>"`. FE nên split bằng `: ` để tách field ra; fallback hiển thị nguyên message nếu không split được.

---

## 5. Đề xuất UI/UX

### 5.1 Layout đề xuất cho màn "Automation Settings"

```
┌─────────────────────────────────────────────────────────────┐
│  Automation Settings                                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ╔═ FAN (Quạt) ═══════════════════════════════════════════╗ │
│  ║ Mode:  ◉ AUTO   ○ MANUAL              State: ● OFF     ║ │
│  ║ ────────────────────────────────────────────────────── ║ │
│  ║ Ngưỡng nhiệt độ (°C)                                   ║ │
│  ║                                                        ║ │
│  ║ 0 ──────●═══════════●──────────── 100                  ║ │
│  ║         26          30                                 ║ │
│  ║       Low (tắt)   High (bật)                           ║ │
│  ║                                                        ║ │
│  ║ 💡 Khi nhiệt độ ≤ 26°C → tắt; ≥ 30°C → bật.           ║ │
│  ║                                                        ║ │
│  ║                       [ Reset ]  [ Save ]              ║ │
│  ╚════════════════════════════════════════════════════════╝ │
│                                                             │
│  ╔═ LED (Đèn) ════════════════════════════════════════════╗ │
│  ║ Mode:  ○ AUTO   ◉ MANUAL              State: ● ON      ║ │
│  ║ ────────────────────────────────────────────────────── ║ │
│  ║ ⚠ Đang ở MANUAL — ngưỡng bên dưới sẽ không có tác dụng ║ │
│  ║   cho đến khi bạn chuyển sang AUTO.                    ║ │
│  ║                                                        ║ │
│  ║ Ngưỡng ánh sáng                                        ║ │
│  ║                                                        ║ │
│  ║ 0 ──────●═══════════●──────────── 100                  ║ │
│  ║         50          70                                 ║ │
│  ║       On (bật)    Off (tắt)                            ║ │
│  ║                                                        ║ │
│  ║ 💡 Khi ánh sáng ≤ 50 → bật đèn; ≥ 70 → tắt đèn.       ║ │
│  ║                                                        ║ │
│  ║                       [ Reset ]  [ Save ]              ║ │
│  ╚════════════════════════════════════════════════════════╝ │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Checklist cho FE

- [ ] **Pre-fill** ngưỡng từ `GET /automation/config` khi mount.
- [ ] **Dual-handle slider** (2 nút trên 1 thanh) cho mỗi thiết bị, range `[0, 100]`, step `0.1` hoặc `1` tuỳ design.
- [ ] **Client-side validation** (trùng với backend, để UX nhanh):
  - Cả 2 field bắt buộc, trong `[0, 100]`.
  - FAN: `highTemp >= lowTemp`.
  - LED: `offThreshold >= onThreshold`.
- [ ] **Disable Save** khi không có thay đổi hoặc validation fail.
- [ ] **Show mode hiện tại** + cảnh báo nếu đang `MANUAL` (như mockup 5.1).
- [ ] **Show state hiện tại** (ON/OFF + dot màu xanh/xám) — lấy từ SSE.
- [ ] **Show last command** (`lastCommandReason`, `lastCommandAt`, `lastCommandSource`) ở góc nhỏ → giúp user hiểu vì sao thiết bị vừa đổi.
- [ ] **Subscribe SSE** `/dashboard/stream` để khi save xong, slider + state tự động cập nhật mà không cần re-fetch. Phải có **fallback polling** (`GET /dashboard` mỗi 10s) nếu SSE đứt.
- [ ] **Optimistic UI** không bắt buộc — vì SSE đẩy snapshot mới gần như tức thời sau khi save.
- [ ] **Error toast** khi PUT fail, parse `response.data.message` (xem mục 4).

### 5.3 Edge cases cần handle

| Tình huống | Hành vi đề xuất |
|------------|------------------|
| User kéo `low` qua `high` | Tự động "đẩy" handle còn lại theo (clamp), không cho cross. |
| User nhập số ngoài `[0, 100]` | Clamp vào range, hiện hint nhỏ. |
| Mất kết nối SSE | Show badge "🟡 Reconnecting..." + fallback polling. |
| Token hết hạn giữa lúc save | Interceptor refresh + retry request. |
| `mode = MANUAL` mà user vẫn save threshold | Cho phép save (backend không chặn) nhưng show banner cảnh báo "Ngưỡng đã lưu, nhưng cần bật AUTO để có tác dụng" + nút "Switch to AUTO". |

---

## 6. Worked example (luồng đầy đủ)

User muốn: "Khi nhiệt độ trên 28°C thì tự bật quạt, dưới 25°C thì tắt".

```http
PUT /api/v1/automation/fan-threshold
Authorization: Bearer eyJ...
Content-Type: application/json

{ "lowTemp": 25.0, "highTemp": 28.0 }
```

Response:

```json
{
  "statusCode": 200,
  "message": "Update fan threshold successfully",
  "data": {
    "fanLowTemp": 25.00, "fanHighTemp": 28.00,
    "ledOnThreshold": 50.00, "ledOffThreshold": 70.00,
    "pirAlertCooldownSeconds": 60
  }
}
```

Đồng thời, FE đang subscribe SSE nhận được:

```
event: dashboard.snapshot
data: { "temp": {...}, ..., "fan": { "mode": "AUTO", "state": "OFF", ... }, "automationConfig": { "fanLowTemp": 25.00, "fanHighTemp": 28.00, ... } }
```

→ Slider cập nhật, không cần extra fetch. Khi cảm biến tiếp theo báo `TEMP = 28.5`, backend tự ON quạt và đẩy snapshot mới với `fan.state = "ON"`, `lastCommandReason = "TEMP >= highTemp"`.

---

## 7. Câu hỏi mở cho FE/PM (cần align trước khi build)

1. **Step của slider:** `0.1` (precision cao) hay `1` (UX dễ kéo)? Backend lưu `precision = 5, scale = 2` nên có thể nhận tới 0.01.
2. **Đơn vị `LIGHT`:** hiện tại tài liệu ghi range `0-100`. Cần xác nhận với hardware/PM xem đây có phải `%` hay là raw lux?
3. **Default values:** khi user lần đầu vào, nếu chưa có config nào, backend đã seed sẵn (DDL external) — không cần FE handle case "empty config". Nhưng nên hiển thị nút "Restore defaults" với giá trị nào? Cần PM confirm.
4. **Có nên gộp** 2 nút Save (FAN + LED) thành 1 "Save All" không, hay để riêng từng card? (Khuyến nghị: để riêng — atomic per-device, ít risk hơn.)
5. **PIR cooldown** (`pirAlertCooldownSeconds`) có cần expose lên UI cùng màn này không, hay tách màn "Notifications"?

---

## Phụ lục: Quick reference cho FE

```ts
// Suggested TypeScript types
type DeviceType  = 'LED' | 'FAN';
type DeviceMode  = 'MANUAL' | 'AUTO';
type DeviceState = 'ON' | 'OFF';
type CommandSource = 'MANUAL_USER' | 'AUTOMATION' | 'SYSTEM';

interface AutomationConfig {
  fanLowTemp: number;
  fanHighTemp: number;
  ledOnThreshold: number;
  ledOffThreshold: number;
  pirAlertCooldownSeconds: number;
}

interface DeviceStatus {
  deviceType: DeviceType;
  mode: DeviceMode;
  state: DeviceState;
  lastCommandPayload: string | null;
  lastCommandSource: CommandSource | null;
  lastCommandReason: string | null;
  lastCommandAt: string | null;  // ISO 8601
  updatedAt: string;             // ISO 8601
}

interface BaseResponse<T> {
  statusCode: number;
  message: string;
  data: T | null;
}

interface UpdateFanThresholdRequest {
  lowTemp: number;
  highTemp: number;
}

interface UpdateLedThresholdRequest {
  onThreshold: number;
  offThreshold: number;
}
```
