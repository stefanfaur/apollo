// Device-side status tracking
export type LockState = 'locked' | 'unlocked';
export type EnrollStatus = 'idle' | 'in_progress' | 'success' | 'failure';

export interface Device {
  id: string;
  isActive: boolean;
  deviceType: string;
  lastMessageTime?: string;
  lockState?: LockState;
  enrollStatus?: EnrollStatus;
}

export interface NotificationPayload {
  hardwareId: string;
  title: string;
  message: string;
  mediaUrl?: string;
}

export interface HelloPayload {
  hardwareId: string;
  deviceType: string;
}

export interface PresignedUrlResponse {
  url: string;
  objectName: string;
}

// New MQTT payloads coming **from** backend to the simulator
export interface UnlockCommand {
  hardwareId: string;
}

export interface EnrollStartPayload {
  hardwareId: string;
  user_fp_id: number;
}

export interface CommandLogEntry {
  timestamp: number;
  topic: string;
  payload: any;
}

// Sensor event types matching STM32/AMB82 firmware
export enum SensorEventType {
  MOTION_DETECTED = 0x01,
  DOOR_OPENED = 0x02,        // Door frame reed switch
  DOOR_OPENED_2 = 0x03,      // Door handle reed switch
  DOOR_OPENED_UNAUTH = 0x04,
  FINGERPRINT_FAILURE = 0x05,
  UNKNOWN = 0xFF
}

export interface SensorEvent {
  type: SensorEventType;
  description: string;
  requiresMedia: boolean;
}
