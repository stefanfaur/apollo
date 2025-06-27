# Apollo MQTT Message Reference

This document describes all MQTT topics and message structures used in the Apollo smart home security system. The system uses MQTT for communication between hardware devices (AMB82/STM32), backend services, and the device simulator.

## MQTT Broker Configuration

- **Host**: `localhost` for local and `faur.sh` for deployment.
- **Port**: `1883` (standard MQTT)
- **WebSocket Port**: `1884` (for browser-based simulator)
- **QoS**: 0 (fire-and-forget) for all messages *TODO: get to improve this* 
- **Encoding**: UTF-8 JSON

## Topic Overview

| Topic | Direction | Publisher | Subscriber | Purpose |
|-------|-----------|-----------|------------|---------|
| `devices/hello` | **Publish** | Hardware, Simulator | Backend | Device registration/announcement |
| `devices/notifications` | **Publish** | Hardware, Simulator | Backend | Event notifications with optional media |
| `devices/commands/unlock` | **Subscribe** | Backend | Hardware, Simulator | Remote unlock commands |
| `doorlock/1/enroll/start` | **Subscribe** | Backend | Hardware, Simulator | Start fingerprint enrollment |
| `doorlock/1/enroll/status` | **Publish** | Hardware, Simulator | Backend | Fingerprint enrollment results |
| `doorlock/1/event` | **Publish** | Hardware, Simulator | Backend | Door-specific events |

## Message Structures

### 1. Device Hello (`devices/hello`)

**Purpose**: Announces device presence and registration information when connecting to MQTT broker.

**Publisher**: Hardware devices, Device simulator  
**Subscriber**: Notification service (backend)

**Message Structure**:
```json
{
  "hardwareId": "AMB82_001",
  "deviceType": "AMB82"
}
```

**Fields**:
- `hardwareId` (string, required): Unique identifier for the physical device
- `deviceType` (string, required): Device type identifier (e.g., "AMB82", "STM32")

**Example**:
```json
{
  "hardwareId": "AMB82_001",
  "deviceType": "AMB82"
}
```

### 2. Device Notifications (`devices/notifications`)

**Purpose**: General event notifications from devices, including sensor events, security alerts, and media recordings.

**Publisher**: Hardware devices, Device simulator  
**Subscriber**: Notification service (backend)

**Message Structure**:
```json
{
  "hardwareId": "AMB82_001",
  "title": "Motion Detected",
  "message": "Motion detected near the door",
  "mediaUrl": "uploads/video_20250101_120000.mp4",
  "eventType": "MOTION_DETECTED",
  "timestamp": "1704110400000"
}
```

**Fields**:
- `hardwareId` (string, required): Device identifier
- `title` (string, required): Human-readable event title
- `message` (string, required): Detailed event description
- `mediaUrl` (string, optional): Path to associated media file in MinIO storage
- `eventType` (string, optional): Event type for backend categorization
- `timestamp` (string, optional): Timestamp in milliseconds (hardware only)

**Common Event Types**:
- `MOTION_DETECTED`: Motion sensor triggered
- `DOOR_OPENED`: Door opened (authorized)
- `DOOR_OPENED_UNAUTH`: Door opened (unauthorized)
- `FINGERPRINT_FAILURE`: Fingerprint authentication failed
- `HANDLE_TRIED`: Door handle tried

### 3. Unlock Commands (`devices/commands/unlock`)

**Purpose**: Remote unlock commands sent from backend to devices.

**Publisher**: Backend services (via notification service)  
**Subscriber**: Hardware devices, Device simulator

**Message Structure**:
```json
{
  "hardwareId": "AMB82_001"
}
```

**Fields**:
- `hardwareId` (string, required): Target device identifier

**Behavior**:
- Devices ignore commands not matching their hardwareId
- Hardware forwards unlock command to STM32 via serial protocol
- Simulator updates device state and UI

### 4. Fingerprint Enrollment Start (`doorlock/1/enroll/start`)

**Purpose**: Initiates fingerprint enrollment process on target device.

**Publisher**: Backend services (via notification service)  
**Subscriber**: Hardware devices, Device simulator

**Message Structure**:
```json
{
  "hardwareId": "AMB82_001",
  "user_fp_id": 23
}
```

**Fields**:
- `hardwareId` (string, required): Target device identifier
- `user_fp_id` (integer, required): Fingerprint template ID (1-127)

**Behavior**:
- Devices validate hardwareId before processing
- Hardware forwards enrollment command to STM32 fingerprint sensor
- Simulator simulates enrollment process with UI feedback

### 5. Fingerprint Enrollment Status (`doorlock/1/enroll/status`)

**Purpose**: Reports fingerprint enrollment results back to backend.

**Publisher**: Hardware devices, Device simulator  
**Subscriber**: Backend services (notification service)

**Success Message**:
```json
{
  "hardwareId": "AMB82_001",
  "eventType": "EnrollSuccess",
  "description": "FP enroll success id 17",
  "mediaUrl": "",
  "timestamp": "1704110400000"
}
```

**Failure Message**:
```json
{
  "hardwareId": "AMB82_001",
  "eventType": "EnrollFailure",
  "description": "FP enroll failure code 0x02",
  "mediaUrl": "",
  "timestamp": "1704110400000"
}
```

**Fields**:
- `hardwareId` (string, required): Device identifier
- `eventType` (string, required): "EnrollSuccess" or "EnrollFailure"
- `description` (string, required): Success includes template ID, failure includes error code
- `mediaUrl` (string): Always empty for enrollment status
- `timestamp` (string, optional): Timestamp in milliseconds

**Error Codes**:
- `0x01`: Timeout waiting for finger
- `0x02`: Fingerprints did not match
- `0x03`: Storing template failed
- `0x04`: Sensor/imaging error

### 6. Door Events (`doorlock/1/event`)

**Purpose**: Door-specific events like fingerprint unlocks.

**Publisher**: Hardware devices, Device simulator  
**Subscriber**: Backend services (notification service)

**Message Structure**:
```json
{
  "hardwareId": "AMB82_001",
  "eventType": "UnlockedFP",
  "title": "Unlocked by Fingerprint",
  "description": "Door unlocked using fingerprint template 5",
  "mediaUrl": "",
  "timestamp": "1704110400000"
}
```

**Fields**: Same as notification messages
**Common Event Types**:
- `UnlockedFP`: Door unlocked via fingerprint
- `UnlockedRFID`: Door unlocked via RFID card
- `UnlockedRemote`: Door unlocked via remote command

## Backend Processing

### Notification Service Subscriptions

The notification service subscribes to:
- `devices/hello` → Registers/updates device in database
- `devices/notifications` → Processes events, analyzes media, creates notifications
- `doorlock/+/enroll/status` → Updates enrollment status in device service

### Message Flow

1. **Device Registration**:
   - Device publishes to `devices/hello`
   - Backend registers device in database

2. **Event Notification**:
   - Device publishes to `devices/notifications`
   - Backend analyzes media (if present) using AI service
   - Backend creates notification for home users

3. **Remote Commands**:
   - Backend publishes to `devices/commands/unlock`
   - Device processes command and unlocks door

4. **Fingerprint Enrollment**:
   - Backend publishes to `doorlock/1/enroll/start`
   - Device starts enrollment process
   - Device publishes result to `doorlock/1/enroll/status`
   - Backend updates device enrollment status

## Simulator Implementation

The device simulator implements all MQTT topics for testing:

- **WebSocket Connection**: Connects to MQTT broker via WebSocket (port 1884)
- **Device State Management**: Maintains lock state and enrollment status
- **Media Simulation**: Uses local video files for testing media uploads
- **Command Logging**: Logs all incoming MQTT commands for debugging
- **Auto-Simulation**: Can automatically generate sensor events for testing

## Hardware Implementation

### AMB82 (Realtek)
- **WiFi Connection**: Connects to MQTT broker via WiFi
- **Serial Communication**: Forwards commands to STM32 via serial protocol
- **Media Handling**: Records and uploads video to MinIO storage
- **Non-blocking**: Uses non-blocking MQTT operations to maintain responsiveness

### STM32 (BlackPill)
- **Serial Protocol**: Receives commands from AMB82
- **Fingerprint Sensor**: Manages fingerprint enrollment and authentication
- **Door Control**: Controls door lock mechanism
- **Sensor Monitoring**: Monitors door sensors and motion detection

## Security Considerations

- **Hardware ID Validation**: All commands validate hardwareId before processing
- **Command Filtering**: Devices ignore commands not intended for them
- **Media Security**: Media URLs use MinIO presigned URLs for secure access
- **Connection Security**: MQTT connections should use TLS in production

## Development and Testing

### MQTT Debugging Tools

1. **MQTT Watcher** (`utils/mqtt-watcher/watcher.py`):
   - Monitors all MQTT traffic
   - Useful for debugging message flow
   - Run with `uv run watcher.py` or if you don't have `uv` use a `venv`.

2. **Device Simulator** (`utils/device-simulator/apollo-sim/`):
   - Simulates all device behaviors
   - Includes media upload testing

3. **MQTT Test Publisher** (`utils/mqtt-watcher/publish-test.py`):
   - Publishes test messages to various topics
   - For simple smoke testing

---

*Last updated: 2025-01-27* 